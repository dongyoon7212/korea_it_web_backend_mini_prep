package com.korit.BoardStudyPrep.service;

import com.korit.BoardStudyPrep.dto.ApiRespDto;
import com.korit.BoardStudyPrep.dto.auth.SigninReqDto;
import com.korit.BoardStudyPrep.dto.auth.SignupReqDto;
import com.korit.BoardStudyPrep.entity.User;
import com.korit.BoardStudyPrep.entity.UserRole;
import com.korit.BoardStudyPrep.repository.UserRepository;
import com.korit.BoardStudyPrep.repository.UserRoleRepository;
import com.korit.BoardStudyPrep.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Exception 클래스를 포함한 모든 예외 발생 시 롤백되도록 설정
    @Transactional(rollbackFor = Exception.class)
    public ApiRespDto<?> signup(SignupReqDto signupReqDto) {
        Optional<User> userByUsername = userRepository.getUserByUsername(signupReqDto.getUsername());
        if (userByUsername.isPresent()) {
            return new ApiRespDto<>("failed", "이미 존재하는 아이디 입니다.", null);
        }
        Optional<User> userByEmail = userRepository.getUserByEmail(signupReqDto.getEmail());
        if (userByEmail.isPresent()) {
            return new ApiRespDto<>("failed", "이미 존재하는 이메일 입니다.", null);
        }

        try {
            // 사용자 정보 추가
            Optional<User> optionalUser = userRepository.addUser(signupReqDto.toEntity(bCryptPasswordEncoder));

            // 사용자 추가에 실패했을 경우 (예: 데이터베이스 제약 조건 위반 등)
            if (optionalUser.isEmpty()) {
                // RuntimeException을 발생시켜 트랜잭션을 롤백하도록 유도
                throw new RuntimeException("회원 정보 추가에 실패했습니다.");
            }

            // 사용자 역할(Role) 추가
            UserRole userRole = UserRole.builder()
                    .userId(optionalUser.get().getUserId())
                    .roleId(3) // 일반 사용자 역할 ID (예시)
                    .build();
            userRoleRepository.addUserRole(userRole);

            // 모든 작업이 성공적으로 완료되면 성공 응답 반환
            return new ApiRespDto<>("success", "회원가입 성공", optionalUser.get());

        } catch (Exception e) {
            // 트랜잭션 내에서 예외 발생 시 자동으로 롤백됩니다.
            // 클라이언트에게 실패 응답 반환
            return new ApiRespDto<>("failed", "회원가입 중 오류가 발생했습니다: " + e.getMessage(), null);
        }
    }

    public ApiRespDto<?> signin(SigninReqDto signinReqDto) {
        Optional<User> userByUsername = userRepository.getUserByUsername(signinReqDto.getUsername());
        if (userByUsername.isEmpty()) {
            return new ApiRespDto<>("failed", "사용자 정보를 확인하세요.", null);
        }

        if (!bCryptPasswordEncoder.matches(userByUsername.get().getPassword(), signinReqDto.getPassword())) {
            return new ApiRespDto<>("failed", "사용자 정보를 확인하세요.", null);
        }

        String accessToken = jwtUtil.generateAccessToken(userByUsername.get().getUserId().toString());
        return new ApiRespDto<>("success", "로그인 성공", accessToken);
    }

}
