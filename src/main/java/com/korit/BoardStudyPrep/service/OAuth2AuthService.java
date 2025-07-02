package com.korit.BoardStudyPrep.service;


import com.korit.BoardStudyPrep.dto.ApiRespDto;
import com.korit.BoardStudyPrep.dto.oauth2.OAuth2MergeReqDto;
import com.korit.BoardStudyPrep.dto.oauth2.OAuth2SignupReqDto;
import com.korit.BoardStudyPrep.entity.User;
import com.korit.BoardStudyPrep.entity.UserRole;
import com.korit.BoardStudyPrep.repository.OAuth2UserRepository;
import com.korit.BoardStudyPrep.repository.UserRepository;
import com.korit.BoardStudyPrep.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OAuth2AuthService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuth2UserRepository oAuth2UserRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public ApiRespDto<?> mergeAccount(OAuth2MergeReqDto oAuth2MergeReqDto) {
        Optional<User> optionalUser = userRepository.getUserByUsername(oAuth2MergeReqDto.getUsername());
        if(optionalUser.isEmpty()) {
            return new ApiRespDto<>("failed", "사용자 정보를 확인하세요.", null);
        }
        if (!passwordEncoder.matches(oAuth2MergeReqDto.getPassword(), optionalUser.get().getPassword())) {
            return new ApiRespDto<>("failed", "사용자 정보를 확인하세요.", null);
        }

        oAuth2UserRepository.insertOAuth2User(oAuth2MergeReqDto.toOAuth2User(optionalUser.get().getUserId()));

        return new ApiRespDto<>("success", "계정 통합 성공", null);

    }

    @Transactional(rollbackFor = Exception.class)
    public ApiRespDto<?> signup(OAuth2SignupReqDto oAuth2SignupReqDto) {
        Optional<User> userByUsername = userRepository.getUserByUsername(oAuth2SignupReqDto.getUsername());
        if (userByUsername.isPresent()) {
            return new ApiRespDto<>("failed", "이미 존재하는 아이디 입니다.", null);
        }

        Optional<User> userByEmail = userRepository.getUserByEmail(oAuth2SignupReqDto.getEmail());
        if (userByEmail.isPresent()) {
            return new ApiRespDto<>("failed", "이미 존재하는 이메일 입니다.", null);
        }

        try {
            Optional<User> user = userRepository.addUser(oAuth2SignupReqDto.toEntity(passwordEncoder));
            if (user.isEmpty()) {
                throw new RuntimeException("회원 정보 추가에 실패했습니다.");
            }

            UserRole userRole = UserRole.builder()
                    .userId(user.get().getUserId())
                    .roleId(3)
                    .build();
            userRoleRepository.addUserRole(userRole);
            oAuth2UserRepository.insertOAuth2User(oAuth2SignupReqDto.toOAuth2User(user.get().getUserId()));

            return new ApiRespDto<>("success", "OAuth2 회원가입 성공", null);
        } catch (Exception e) {
            // 트랜잭션 내에서 예외 발생 시 자동으로 롤백됩니다.
            // 클라이언트에게 실패 응답 반환
            return new ApiRespDto<>("failed", "회원가입 중 오류가 발생했습니다: " + e.getMessage(), null);
        }
    }
}
