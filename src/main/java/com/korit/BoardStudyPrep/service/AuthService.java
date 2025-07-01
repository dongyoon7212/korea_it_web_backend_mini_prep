package com.korit.BoardStudyPrep.service;

import com.korit.BoardStudyPrep.dto.ApiRespDto;
import com.korit.BoardStudyPrep.dto.SignupReqDto;
import com.korit.BoardStudyPrep.entity.User;
import com.korit.BoardStudyPrep.entity.UserRole;
import com.korit.BoardStudyPrep.repository.UserRepository;
import com.korit.BoardStudyPrep.repository.UserRoleRepository;
import com.korit.BoardStudyPrep.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    public ApiRespDto<?> signup(SignupReqDto signupReqDto) {
        Optional<User> optionalUser = userRepository.addUser(signupReqDto.toEntity(bCryptPasswordEncoder));
        UserRole userRole = UserRole.builder()
                .userId(optionalUser.get().getUserId())
                .roleId(3)
                .build();
        userRoleRepository.addUserRole(userRole);
        return new ApiRespDto<>("success", "추가 성공", optionalUser);
    }

}
