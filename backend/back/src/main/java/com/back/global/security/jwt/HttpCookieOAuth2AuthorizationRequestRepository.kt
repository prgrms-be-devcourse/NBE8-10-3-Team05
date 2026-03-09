package com.back.global.security.jwt

import com.back.domain.member.member.service.AuthCookieService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class HttpCookieOAuth2AuthorizationRequestRepository(
    private val authCookieService: AuthCookieService,
    // application.yml의 custom.accessToken.expirationSeconds 값을 주입 (기본값 2000초)
    @Value("\${custom.accessToken.expirationSeconds:2000}")
    private val cookieExpireSeconds: Int
) : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    companion object {
        const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return request.cookies?.find { it.name == OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME }
            ?.let { authCookieService.deserialize(it.value, OAuth2AuthorizationRequest::class.java) }
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            response.addHeader("Set-Cookie", authCookieService.deleteCookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME))
            response.addHeader("Set-Cookie", authCookieService.deleteCookie(REDIRECT_URI_PARAM_COOKIE_NAME))
            return
        }

        // 객체를 String으로 변환 후 쿠키 생성
        val serializedRequest = authCookieService.serialize(authorizationRequest)
        response.addHeader("Set-Cookie", authCookieService.createCookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serializedRequest, cookieExpireSeconds))

        // 리다이렉트 URI 저장
        request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)?.takeIf { it.isNotBlank() }?.let {
            response.addHeader("Set-Cookie", authCookieService.createCookie(REDIRECT_URI_PARAM_COOKIE_NAME, it, cookieExpireSeconds))
        }
    }

    override fun removeAuthorizationRequest(request: HttpServletRequest, response: HttpServletResponse): OAuth2AuthorizationRequest? {
        val result = this.loadAuthorizationRequest(request)
        // 사용이 끝난 쿠키는 모두 삭제 (리다이렉트 URI 쿠키 포함)
        response.addHeader("Set-Cookie", authCookieService.deleteCookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME))
        response.addHeader("Set-Cookie", authCookieService.deleteCookie(REDIRECT_URI_PARAM_COOKIE_NAME))
        return result
    }
}
