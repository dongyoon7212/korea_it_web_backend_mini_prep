package com.korit.BoardStudyPrep.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OAuth2User {
    private int oauthUserIdId;
    private int userId;
    private String provider;
    private String providerUserId;
    private LocalDateTime createDt;
    private LocalDateTime updateDt;
}
