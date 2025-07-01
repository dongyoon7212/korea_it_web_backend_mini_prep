package com.korit.BoardStudyPrep.repository;

import com.korit.BoardStudyPrep.entity.OAuth2User;
import com.korit.BoardStudyPrep.mapper.OAuth2UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OAuth2UserRepository {

    @Autowired
    private OAuth2UserMapper oAuth2UserMapper;

    public OAuth2User findByProviderAndProviderUserId(String provider, String providerUserId) {
        return oAuth2UserMapper.getOAuth2UserByProviderAndProviderUserId(provider, providerUserId);
    }
    public int insertOAuth2User(OAuth2User oAuth2User) {
        return oAuth2UserMapper.addOAuth2User(oAuth2User);
    }
}
