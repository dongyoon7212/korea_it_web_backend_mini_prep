package com.korit.BoardStudyPrep.security.handler;

import com.korit.BoardStudyPrep.entity.OAuth2User;
import com.korit.BoardStudyPrep.entity.User;
import com.korit.BoardStudyPrep.mapper.OAuth2UserMapper;
import com.korit.BoardStudyPrep.mapper.UserMapper;
import com.korit.BoardStudyPrep.security.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OAuth2UserMapper oAuth2UserMapper;

    @Value("${client.deploy-address}")
    private String clientAddress;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. OAuth2User 정보 가져오기 (provider, id, email)
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String provider = defaultOAuth2User.getAttribute("provider");
        String providerUserId = defaultOAuth2User.getAttribute("id");
        String email = defaultOAuth2User.getAttribute("email");

        // 2. provider + providerUserId로 연동된 사용자 있는지 DB 조회 (oauth2_user_tb)
        Optional<OAuth2User> optionalOAuth2User = oAuth2UserMapper.getOAuth2UserByProviderAndProviderUserId(provider, providerUserId);

        // OAuth2 로그인을 통해 회원가입이 되어있지 않은 상태 (연동이 된적이 없는 경우)
        // OAuth2 동기화
        if (optionalOAuth2User.isEmpty()) {
            // oAuth2User 않은 경우: 프론트로 provider 정보와 providerUserId 전달
            response.sendRedirect(clientAddress + "/auth/oauth2?provider=" + provider + "&providerUserId=" + providerUserId + "&email=" + email);
            return;
        }

        // 4. 연동된 사용자가 있다면 → userId 기준으로 회원 정보 조회
        Optional<User> optionalUser = userMapper.getUserByUserId(optionalOAuth2User.get().getUserId());

        // OAuth2 로그인을 통해 회원가입을 진행한 기록이 있는지 (연동이 된 경우)
        // 5. 사용자 정보가 있을 경우 → JWT Access Token 발급
        String accessToken = null;
        if(optionalUser.isPresent()) {
            accessToken = jwtUtil.generateAccessToken(Integer.toString(optionalUser.get().getUserId()));
        }
        // 6. 최종적으로 accessToken을 쿼리 파라미터로 프론트에 전달
        response.sendRedirect(clientAddress + "/auth/oauth2/signin?accessToken=" + accessToken);
    }
}
