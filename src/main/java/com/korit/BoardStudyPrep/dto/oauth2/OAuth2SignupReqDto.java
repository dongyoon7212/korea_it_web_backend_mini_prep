package com.korit.BoardStudyPrep.dto.oauth2;


import com.korit.BoardStudyPrep.entity.OAuth2User;
import com.korit.BoardStudyPrep.entity.User;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
public class OAuth2SignupReqDto {
    private String email;
    private String username;
    private String password;
    private String provider;
    private String providerUserId;

    public User toEntity(BCryptPasswordEncoder passwordEncoder) {
        return User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .build();
    }

    public OAuth2User toOAuth2User(int userId) {
        return OAuth2User.builder()
                .userId(userId)
                .provider(provider)
                .providerUserId(providerUserId)
                .build();
    }
}
