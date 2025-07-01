package com.korit.BoardStudyPrep.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key KEY;

    //key주입
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateAccessToken(String id) {
        return Jwts.builder()
                .subject("AccessToken") //토큰의 용도를 설명하는 식별자 역할
                .id(id) //토큰에 고유한 식별자를 부여(보통 사용자 ID, 이메일) => 나중에 토큰 무효화나 사용자 조회 시 활용 가능
                .expiration(new Date(new Date().getTime() + (1000L * 60L * 60L * 24L * 30L))) //JWT의 exp (만료 시간) 클레임 설정
                //현재 시간 기준으로 30일 뒤까지 유효한 토큰
                //1000 = 1초를 밀리초로
                //60 * 60 * 24 * 30 = 30일
                .signWith(KEY)// 토큰에 서명(Signature)을 적용
                .compact();//설정한 JWT 내용을 바탕으로 최종적으로 문자열(String) 형태의 JWT 생성
    }

    public String generateMailVerifyToken(String id) {
        return Jwts.builder()
                .subject("VerifyToken")
                .id(id)
                .expiration(new Date(new Date().getTime() + (1000L * 60L * 3L)))
                .signWith(KEY)
                .compact();
    }

    //bearer 토큰이 맞는지 확인
    public boolean isBearer(String token) {
        if (token == null) {
            return false;
        }
        if (!token.startsWith("Bearer ")) {
            return false;
        }
        return true;
    }

    //bearer 접두사 제거
    public String removeBearer(String bearerToken) {
        return bearerToken.replaceFirst("Bearer ", "");
    }

    //Claims : JWT의 Payload 영역. 사용자 정보, 만료일자 등 담겨 있음.
    //JwtException : 토큰이 잘못되었을 경우 (위변조, 만료 등) 발생하는 예외
    public Claims getClaims(String token) throws JwtException {
        JwtParserBuilder jwtParserBuilder = Jwts.parser();
        //Jwts.parser()는 JwtParserBuilder 객체를 반환
        //JWT 파서를 구성할 수 있는 빌더 (parser 설정 작업을 체이닝으로 가능하게 함)
        jwtParserBuilder.setSigningKey(KEY);//토큰의 서명을 검증하기 위해 비밀키(KEY) 설정
        JwtParser jwtParser = jwtParserBuilder.build();//설정이 완료된 파서를 .build() 하여 최종 JwtParser 객체 생성
        return jwtParser.parseClaimsJws(token).getBody();//순수 Claims JWT 를 파싱
    }
}
