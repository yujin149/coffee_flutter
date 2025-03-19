package com.shop.api;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OAuth2Service {

    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    public OAuth2Service(JwtUtil jwtUtil , RestTemplate restTemplate) {
        this.jwtUtil = jwtUtil;
        this.restTemplate =  restTemplate;
    }

    public String verifyGoogleToken(String accessToken, String idToken) {
        // Google API를 호출하여 사용자 정보 가져오기
        String googleUserInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // accessToken을 Bearer 토큰으로 설정


        HttpEntity<String> entity = new HttpEntity<>(headers);


        ResponseEntity<Map> response = restTemplate.exchange(googleUserInfoUrl, HttpMethod.GET, entity, Map.class);



        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> userInfo = response.getBody();


            // 이메일을 기반으로 JWT 토큰 생성
            String email = (String) userInfo.get("email");
            return jwtUtil.generateToken(email);
        }

        // 검증 실패 시 null 반환
        return null;
    }
}