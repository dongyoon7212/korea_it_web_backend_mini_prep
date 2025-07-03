package com.korit.BoardStudyPrep.repository;

import com.korit.BoardStudyPrep.entity.OAuth2User;
import com.korit.BoardStudyPrep.mapper.OAuth2UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OAuth2UserRepository {

    @Autowired
    private OAuth2UserMapper oAuth2UserMapper;

    public Optional<OAuth2User> getOAuth2UserByProviderAndProviderUserId(String provider, String providerUserId) {
        return oAuth2UserMapper.getOAuth2UserByProviderAndProviderUserId(provider, providerUserId);
    }

    public Optional<OAuth2User> getOAuth2UserByUserId(Integer userId) {
        return oAuth2UserMapper.getOAuth2UserByUserId(userId);
    }

    public int insertOAuth2User(OAuth2User oAuth2User) {
        return oAuth2UserMapper.addOAuth2User(oAuth2User);
    }
}
