package com.shop.api;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
//    private final String SECRET_KEY = "your_secret_key";  // JWT 비밀키 (보안 중요!)

    // 강력한 비밀키 생성 (HS256에 적합한 크기)
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // JWT 토큰 생성
    public String generateToken(String userid) {
        System.out.println("token 발급 userid: "+userid);
        String token = Jwts.builder()
            .setSubject(userid)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // 1시간 유효
            .signWith(key)
            .compact();

        System.out.println("Generated Token: " + token);  // 생성된 토큰 출력

        return token;
    }

    // JWT 토큰에서 userid 추출
    public String extractUserid(String token) {
        return getClaims(token).getSubject();
    }

    // JWT 토큰 검증
    public boolean validateToken(String token) {
        return getClaims(token).getExpiration().after(new Date());
    }

    // JWT 토큰 해석 (Claims 가져오기)
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)  // 강력한 비밀키로 서명된 토큰을 파싱
            .build()
            .parseClaimsJws(token)
            .getBody();

    }
}
