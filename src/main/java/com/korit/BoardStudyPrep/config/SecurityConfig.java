package com.korit.BoardStudyPrep.config;


import com.korit.BoardStudyPrep.security.filter.JwtAuthenticationFilter;
import com.korit.BoardStudyPrep.security.handler.OAuth2SuccessHandler;
import com.korit.BoardStudyPrep.service.OAuth2PrincipalUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2PrincipalUserService oAuth2PrincipalUserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    //비밀번호를 안전하게 암호화(해싱)하고, 검증하는 역할
    //단방향 해시	복호화 불가능 (암호화된 값을 원래대로 되돌릴 수 없음)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //corsConfigurationSource() 설정은 **Spring Security에서 CORS(Cross-Origin Resource Sharing)**를 처리하기 위한 설정
    //CORS(Cross-Origin Resource Sharing)
    //"브라우저가 보안상 다른 도메인의 리소스 요청을 제한"하는 정책
    //기본적으로 브라우저는 "같은 출처(Same-Origin)"만 허용합니다.
    //출처(origin) = 프로토콜 + 도메인 + 포트
    //예:
    //http://localhost:3000 (프론트)
    //http://localhost:8080 (백엔드)
    //→ 서로 다른 포트이므로 "다른 출처"입니다 → 브라우저가 보안상 차단
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 요청을 보내는 쪽의 도메인(사이트 주소)
        corsConfiguration.addAllowedOriginPattern(CorsConfiguration.ALL);
        // 요청을 보내는 쪽에서 Request, Response HEADER 정보에 대한 제약
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        // 요청을 보내는 쪽의 메소드(GET, POST, PUT, DELETE, OPTION 등)
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);

        // 요청 URL (/api/users)에 대한 CORS 설정 적용을 위해 객체생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); //위에서 설정했던 CorsConfiguration의 내용들이 들어가있음
        source.registerCorsConfiguration("/**", corsConfiguration);// 모든 URL(/**)에 대해 위에서 만든 CORS 정책을 적용
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());   // 위에서 만든 cors 설정(bean) security에 적용
        http.csrf(csrf -> csrf.disable());                  // 서버사이드 렌더링 방식이 아니니 REST API 방식에서는 비활성화
        //CSRF란?
        //사용자가 의도하지 않은 요청을 공격자가 유도해서 서버에 전달하도록 하는 공격
        //사용자가 이미 어떤 사이트(A)에 로그인해서 세션이 유지 중인 상태
        //공격자가 만든 악성 사이트(B)에 사용자가 접속함
        //사이트 B에서 A 사이트로 요청을 몰래 전송
        //사용자의 세션 쿠키가 자동으로 포함되어 요청이 전송됨
        //→ 사용자는 의도하지 않았지만 계정이 삭제됨
        //Spring Security는 기본적으로 CSRF 보호가 활성화되어 있음
        //→ 모든 stateful POST/PUT/DELETE 요청에 대해 CSRF 토큰을 요구함.
        //JWT 방식 또는 무상태(Stateless) 인증이기 때문
        //세션이 없고, 쿠키도 안 쓰고, 토큰 기반이기 때문에 CSRF 공격 자체가 성립되지 않음
        http.formLogin(formLogin -> formLogin.disable());   // 서버사이드 렌더링 로그인 방식 비활성화
        http.httpBasic(httpBasic -> httpBasic.disable());   // HTTP 프로토콜 기본 로그인 방식 비활성화
        http.logout(logout -> logout.disable());            // 서버사이드 렌더링 로그아웃 방식 비활성화

        http.sessionManagement(Session -> Session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

//        http.addFilterBefore(studyFilter, UsernamePasswordAuthenticationFilter.class); // 여기서 jwt 토큰을 통해 인증하는 필터로 대체 할거임
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); //jwt필터 동작 기점을 위해 기점으로 사용
        //UsernamePasswordAuthenticationFilter은 formLogin기반으로 작동됨 그래서 있으나 없으나 무의미한 필터 => 작동안함
        //그 전에 jwtAuthenticationFilter에서 토큰 확인 후 인증 객체 등록

        // 특정 요청 URL에 대한 권한 설정
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/auth/test").hasRole("ADMIN");
            //권한을 ROLE_ADMIN, ROLE_USER처럼 저장했다면 → hasRole("ADMIN") 사용 가능
            //권한을 그냥 ADMIN, USER로 저장했다면 → hasAuthority("ADMIN") 사용
            auth.requestMatchers("/auth/signin", "/auth/signup", "/oauth2/**",
                    "/login/oauth2/**", "/mail/verify").permitAll();
            auth.anyRequest().authenticated();
        });

        //요청이 들어오면 Spring Security의 FilterChain을 탄다
        //→ FilterChainProxy → 여러 필터 중 하나가 OAuth2 요청을 감지함
        //OAuth2AuthorizationRequestRedirectFilter
        //경로가 /oauth2/authorization/{registrationId}이면 이 필터가 작동
        //이 필터는 해당 provider의 로그인 페이지로 리디렉션 시킴

        http.oauth2Login(oauth2 -> oauth2
                // ✅ OAuth2 로그인 요청이 성공하고 사용자 정보 가져오는 과정 설정 시작
                .userInfoEndpoint(userInfo ->
                        // ✅ 사용자 정보 요청이 완료되면 이 커스텀 서비스로 OAuth2User를 처리하겠다는 설정
                        userInfo.userService(oAuth2PrincipalUserService)
                )
                // ✅ OAuth2 인증이 최종적으로 성공한 이후 (사용자 정보 파싱 끝난 후) 실행할 핸들러 지정
                .successHandler(oAuth2SuccessHandler)
        );

        // HttpSecurity 객체에 설정한 모든 정보를 기반으로 build하여 SecurityFilterChain 객체 생성 후 Bean 등록
        return http.build();
    }
}
