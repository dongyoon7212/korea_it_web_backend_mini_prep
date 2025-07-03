package com.korit.BoardStudyPrep.mapper;


import com.korit.BoardStudyPrep.entity.OAuth2User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface OAuth2UserMapper {
    Optional<OAuth2User> getOAuth2UserByProviderAndProviderUserId(String provider, String providerUserId);
    Optional<OAuth2User> getOAuth2UserByUserId(Integer userId);
    int addOAuth2User(OAuth2User oAuth2User);
}
