package com.korit.BoardStudyPrep.dto.oauth2;

import com.korit.BoardStudyPrep.entity.OAuth2User;
import lombok.Data;

@Data
public class OAuth2MergeReqDto {
    private String username;
    private String password;
    private String provider;
    private String providerUserId;

    public OAuth2User toOAuth2User(int userId) {
        return OAuth2User.builder()
                .userId(userId)
                .provider(provider)
                .providerUserId(providerUserId)
                .build();
    }
}
