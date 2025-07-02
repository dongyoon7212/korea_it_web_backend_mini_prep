package com.korit.BoardStudyPrep.service;

import com.korit.BoardStudyPrep.dto.ApiRespDto;
import com.korit.BoardStudyPrep.dto.account.ChangePasswordReqDto;
import com.korit.BoardStudyPrep.entity.User;
import com.korit.BoardStudyPrep.repository.UserRepository;
import com.korit.BoardStudyPrep.security.model.PrincipalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public ApiRespDto<?> changePassword(ChangePasswordReqDto changePasswordReqDto, PrincipalUser principalUser) {
        Optional<User> userByUserId = userRepository.getUserByUserId(principalUser.getUserId());
        if (userByUserId.isEmpty()) {
            return new ApiRespDto<>("failed", "존재하지 않은 사용자 입니다.", null);
        }

        if (!Objects.equals(changePasswordReqDto.getUserId(), principalUser.getUserId())) {
            return new ApiRespDto<>("failed", "잘못된 요청입니다.", null);
        }

        if (!bCryptPasswordEncoder.matches(changePasswordReqDto.getOldPassword(), userByUserId.get().getPassword())) {
            return new ApiRespDto<>("failed", "기존 비밀번호가 일치하지 않습니다.", null);
        }

        //새 비밀번호와 기존 비밀번호가 동일한지 확인 (선택 사항이지만 권장)
        if (bCryptPasswordEncoder.matches(changePasswordReqDto.getNewPassword(), userByUserId.get().getPassword())) {
            return new ApiRespDto<>("failed", "새 비밀번호는 기존 비밀번호와 달라야 합니다.", null);
        }

        int result = userRepository.changePassword(changePasswordReqDto.toEntity(bCryptPasswordEncoder));
        if (result != 1) {
            return new ApiRespDto<>("failed", "문제가 발생했습니다.", null);

        }
        return new ApiRespDto<>("success", "비밀번호 변경이 완료되었습니다.\n다시 로그인 해주세요.", null);
    }
}
