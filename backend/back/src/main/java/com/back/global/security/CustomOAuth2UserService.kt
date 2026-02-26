package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val memberService: MemberService
) : DefaultOAuth2UserService() {

    // 카카오톡 로그인이 성공할 때 마다 이 함수가 실행된다.
    @Transactional
    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        // 카카오 id (yml에서 user-name-attribute: id 설정했으니 name = id)
        val kakaoId = oAuth2User.name // 예: "47121"

        val attributes = HashMap(oAuth2User.attributes)
        val properties = attributes["properties"] as? Map<*, *>

        val nickname = properties?.get("nickname") as? String
        val profileImgUrl = properties?.get("profile_image") as? String

        // 잘 받아와지는지 로그 체크
        // log.info("kakaoId={}, nickname={}, profileImgUrl={}", kakaoId, nickname, profileImgUrl);

        // DB에서 회원 조회 or 생성
        val member: Member = memberService.getOrCreateKakaoMember(kakaoId, nickname, profileImgUrl)

        // SuccessHandler에서 쿠키 발급할 때 memberId가 필요하므로
        // attributes에 우리 memberId를 넣어둔다.
        // TODO: 미리 token을 만드는데 필요한 id, role까지 넣어둔다면 success에서 db를 다시 조회할 필요는 없을 것 같습니다.
        attributes["memberId"] = member.id
        attributes["memberRole"] = member.role
        attributes["memberStatus"] = member.status

        // 권한은 최소 USER로 넣어도 되고, 비워도 되는데
        // 일반적으로는 ROLE_USER 넣는 게 디버깅에 편함
        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            userRequest
                .clientRegistration
                .providerDetails
                .userInfoEndpoint
                .userNameAttributeName // "id"
        )
    }
}
