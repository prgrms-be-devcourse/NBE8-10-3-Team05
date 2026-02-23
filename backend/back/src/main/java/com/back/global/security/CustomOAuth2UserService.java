package com.back.global.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    // 카카오톡 로그인이 성공할 때 마다 이 함수가 실행된다.
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 카카오 id (yml에서 user-name-attribute: id 설정했으니 name = id)
        String kakaoId = oAuth2User.getName(); // 예: "47121"

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        String nickname = properties != null ? (String) properties.get("nickname") : null;
        String profileImgUrl = properties != null ? (String) properties.get("profile_image") : null;

        // 잘 받아와지는지 로그 체크
        // log.info("kakaoId={}, nickname={}, profileImgUrl={}", kakaoId, nickname, profileImgUrl);

        // DB에서 회원 조회 or 생성
        Member member = memberService.getOrCreateKakaoMember(kakaoId, nickname, profileImgUrl);

        // SuccessHandler에서 쿠키 발급할 때 memberId가 필요하므로
        // attributes에 우리 memberId를 넣어둔다.
        // TODO: 미리 token을 만드는데 필요한 id, role까지 넣어둔다면 success에서 db를 다시 조회할 필요는 없을 것 같습니다.
        attributes.put("memberId", member.getId());
        attributes.put("memberRole", member.getRole());
        attributes.put("memberStatus", member.getStatus());

        // 권한은 최소 USER로 넣어도 되고, 비워도 되는데
        // 일반적으로는 ROLE_USER 넣는 게 디버깅에 편함
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userRequest
                        .getClientRegistration()
                        .getProviderDetails()
                        .getUserInfoEndpoint()
                        .getUserNameAttributeName() // "id"
                );
    }
}
