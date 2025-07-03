package com.korit.BoardStudyPrep.service;


import com.korit.BoardStudyPrep.dto.ApiRespDto;
import com.korit.BoardStudyPrep.dto.oauth2.OAuth2MergeReqDto;
import com.korit.BoardStudyPrep.dto.oauth2.OAuth2SignupReqDto;
import com.korit.BoardStudyPrep.entity.OAuth2User;
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

    @Transactional(rollbackFor = Exception.class)
    public ApiRespDto<?> mergeAccount(OAuth2MergeReqDto oAuth2MergeReqDto) {
        //해당 아이디의 회원정보가 존재하고 있는지
        Optional<User> optionalUser = userRepository.getUserByUsername(oAuth2MergeReqDto.getUsername());
        if(optionalUser.isEmpty()) {
            return new ApiRespDto<>("failed", "사용자 정보를 확인하세요.", null);
        }

        User user = optionalUser.get();

        //해당 회원정보가 소셜 계정 연동이 이미 되어있는지
        Optional<OAuth2User> existingOAuth2User = oAuth2UserRepository.getOAuth2UserByUserId(user.getUserId());
        if (existingOAuth2User.isPresent()) {
            return new ApiRespDto<>("failed", "이 계정은 이미 소셜 계정과 연동되어 있습니다.", null);
        }

        //해당 아이디의 비밀번호가 맞는지
        if (!passwordEncoder.matches(oAuth2MergeReqDto.getPassword(), user.getPassword())) {
            return new ApiRespDto<>("failed", "사용자 정보를 확인하세요.", null);
        }

        try { // try-catch 블록 추가하여 예외 발생 시 롤백 및 에러 응답
            int result = oAuth2UserRepository.insertOAuth2User(oAuth2MergeReqDto.toOAuth2User(user.getUserId()));
            if (result != 1) {
                throw new RuntimeException("OAuth2 사용자 정보 통합에 실패했습니다."); // 롤백 유도
            }

            return new ApiRespDto<>("success", "계정 통합 성공", null);
        } catch (Exception e) {
            return new ApiRespDto<>("failed", "계정 통합 중 오류가 발생했습니다: " + e.getMessage(), null);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public ApiRespDto<?> signup(OAuth2SignupReqDto oAuth2SignupReqDto) {
        // 해당 아이디가 이미 존재하는지 확인
        Optional<User> userByUsername = userRepository.getUserByUsername(oAuth2SignupReqDto.getUsername());
        if (userByUsername.isPresent()) {
            return new ApiRespDto<>("failed", "이미 존재하는 아이디 입니다.", null);
        }

        // 해당 이메일이 이미 존재하고 있는지
        Optional<User> userByEmail = userRepository.getUserByEmail(oAuth2SignupReqDto.getEmail());
        if (userByEmail.isPresent()) {
            return new ApiRespDto<>("failed", "이미 존재하는 이메일 입니다.", null);
        }

        try {
            Optional<User> optionalUser = userRepository.addUser(oAuth2SignupReqDto.toEntity(passwordEncoder));
            if (optionalUser.isEmpty()) {
                throw new RuntimeException("회원 정보 추가에 실패했습니다.");
            }

            User user = optionalUser.get();

            UserRole userRole = UserRole.builder()
                    .userId(user.getUserId())
                    .roleId(3)
                    .build();

            int addUserRoleResult = userRoleRepository.addUserRole(userRole);
            if (addUserRoleResult != 1) {
                throw new RuntimeException("OAuth2 사용자 정보 추가에 실패했습니다."); // 롤백 유도
            }

            int oauth2InsertResult = oAuth2UserRepository.insertOAuth2User(oAuth2SignupReqDto.toOAuth2User(user.getUserId()));
            if (oauth2InsertResult != 1) {
                throw new RuntimeException("OAuth2 사용자 정보 추가에 실패했습니다."); // 롤백 유도
            }

            return new ApiRespDto<>("success", "OAuth2 회원가입 성공", user);
        } catch (Exception e) {
            // 트랜잭션 내에서 예외 발생 시 자동으로 롤백됩니다.
            // 클라이언트에게 실패 응답 반환
            return new ApiRespDto<>("failed", "회원가입 중 오류가 발생했습니다: " + e.getMessage(), null);
        }
    }
}
