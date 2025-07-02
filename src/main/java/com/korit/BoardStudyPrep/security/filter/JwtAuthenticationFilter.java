package com.korit.BoardStudyPrep.security.filter;

import com.korit.BoardStudyPrep.entity.User;
import com.korit.BoardStudyPrep.repository.UserRepository;
import com.korit.BoardStudyPrep.security.jwt.JwtUtil;
import com.korit.BoardStudyPrep.security.model.PrincipalUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

//AuthenticationFilter를 대체
//@Component를 붙여서 스프링 빈으로 자동 등록
@Component
public class JwtAuthenticationFilter implements Filter { //Filter 인터페이스를 구현한 서블릿 필터
//Spring Security 필터 체인에 등록되어 요청마다 토큰을 검사하고 인증 정보를 설정하는 역할
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    //서블릿 필터의 핵심 메서드. 모든 요청을 가로채 처리하거나 다음 필터로 넘김
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest; //ServletRequest를 HTTP 요청 객체로 캐스팅
        //해당 메소드가 아니면 그냥 다음 필터로 넘김
        List<String> methods = List.of("POST", "GET", "PUT", "PATCH", "DELETE");
        if (!methods.contains(request.getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //요청 헤더에서 Authorization 값을 꺼냄. (예: "Bearer eyJhbGciOiJIUz...")
        String authorization = request.getHeader("Authorization");
        System.out.println("Bearer 토큰 : " + authorization);
        if (jwtUtil.isBearer(authorization)) { //Authorization 헤더가 "Bearer "로 시작하는지 확인하는 메서드.
            String accessToken = jwtUtil.removeBearer(authorization); //"Bearer " 접두사를 제거하여 순수 토큰 문자열만 남김.
            try {
                Claims claims = jwtUtil.getClaims(accessToken);
                //토큰에서 Claims(사용자 정보) 를 추출.
                //이때 서명 검증도 같이 진행됨.
                //서명 위조나 만료 시 예외 발생.
                String id = claims.getId(); //JWT의 jti 필드(ID)에서 사용자 ID를 추출
                Integer userId = Integer.parseInt(id);
                //UserDetailsService
                Optional<User> optionalUser = userRepository.getUserByUserId(userId); //DB에서 해당 userId로 사용자 조회
                optionalUser.ifPresentOrElse((user) -> { //사용자 존재 여부에 따라 분기 처리
                    //DB에서 조회된 User 객체를 Spring Security 인증 객체(PrincipalUser) 로 변환
                    //User (UserDetails)
                    PrincipalUser principalUser = PrincipalUser.builder()
                            .userId(user.getUserId())
                            .username(user.getUsername())
                            .password(user.getPassword())
                            .email(user.getEmail())
                            .userRoles(user.getUserRoles())
                            .build();
                    //UsernamePasswordAuthenticationToken 직접 생성
                    //인증 객체 생성 (UsernamePasswordAuthenticationToken은 인증 정보/권한을 담는 객체).
                    //비밀번호는 ""으로 비움 (이미 인증된 상태이므로 비교할 필요 없음).
                    Authentication authentication = new UsernamePasswordAuthenticationToken(principalUser, "", principalUser.getAuthorities());
                    //Spring Security의 인증 컨텍스트에 인증 객체 저장 → 이후 요청은 인증된 사용자로 간주됨
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("인증 성공");
                    System.out.println(authentication.getName());
                }, () -> {
                    throw new AuthenticationServiceException("인증 실패"); //DB에 사용자 없으면 인증 실패 예외 발생
                });
            } catch (RuntimeException e) {
                e.printStackTrace();;
            }
        }
        //인증 실패든 성공이든 필터링을 중단하지 않고 다음 필터로 넘김
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
