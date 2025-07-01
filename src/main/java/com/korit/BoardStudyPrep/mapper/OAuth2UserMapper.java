package com.korit.BoardStudyPrep.mapper;


import com.korit.BoardStudyPrep.entity.OAuth2User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuth2UserMapper {
    OAuth2User getOAuth2UserByProviderAndProviderUserId(String provider, String providerUserId);
    int addOAuth2User(OAuth2User oAuth2User);
}
