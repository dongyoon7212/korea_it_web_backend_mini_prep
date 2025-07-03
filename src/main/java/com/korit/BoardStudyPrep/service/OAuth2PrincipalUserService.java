package com.korit.BoardStudyPrep.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// Spring Security에서 기본으로 제공하는 OAuth2UserService를 상속받아 커스터마이징
@Service
public class OAuth2PrincipalUserService extends DefaultOAuth2UserService {

    // OAuth2 로그인 성공 시 호출되는 메서드
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Spring Security가 OAuth2 provider에게 AccessToken으로 사용자 정보를 요청
        // 그리고 그 결과로 받은 사용자 정보(JSON)를 파싱한 객체를 리턴받음
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 사용자 정보(Map 형태) 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 3. 어떤 OAuth2 provider인지 확인 (google, kakao, naver 등)
        // application.yml에서 설정한 registrationId 값이 들어옴

        //provider => 공급처
        String provider = userRequest.getClientRegistration().getRegistrationId();  // google, kakao 등

        // 4. 로그인한 사용자의 식별자(id), 이메일 초기화
        //로그인시 사용한 이메일
        String email = null;
        //공급처에서 발행한 사용자의 식별자
        String id = null;

        // 5. provider 종류에 따라 사용자 정보 파싱 방식이 다르므로 분기 처리
        switch (provider) {
            case "google":
                // Google은 "sub"이 유저 ID, "email" 키에 이메일이 들어있음
                id = attributes.get("sub").toString();
                email = (String) attributes.get("email");
                break;
            case "naver":
                // Naver는 "response" 안에 유저 정보가 있음
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                id = response.get("id").toString();
                email = (String) response.get("email");
                break;
            case "kakao":
                // Kakao는 최상위에 "id", 그리고 "kakao_account" 안에 "email"이 있음
                id = attributes.get("id").toString();
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                email = (String) kakaoAccount.get("email");
                break;
        }

        // 6. 우리가 필요한 정보만 골라 새롭게 attributes 구성 (정리된 형태)
        Map<String, Object> newAttributes = Map.of(
                "id", id,
                "provider", provider,
                "email", email
        );

        // 7. 권한 설정: 임시 권한 부여 (ROLE_TEMPORARY)
        // 실제 권한은 OAuth2SuccessHandler에서 판단
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_TEMPORARY"));

        // 8. Spring Security가 사용할 OAuth2User 객체 생성해서 반환
        // - attributes: 우리가 새로 구성한 사용자 정보
        // - nameAttributeKey: "id" → principal.getName()으로 가져올 때 사용됨
        return new DefaultOAuth2User(authorities, newAttributes, "id");
    }
}
