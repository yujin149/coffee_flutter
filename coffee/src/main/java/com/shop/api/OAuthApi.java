package com.shop.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/oauth2")
public class OAuthApi {

    private final OAuth2Service oauth2Service; // 서비스 추가

    @Autowired
    public OAuthApi(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        String accessToken = request.getAccessToken();
        String idToken = request.getIdToken();

        System.out.println("accessToken : " + accessToken);
        System.out.println("idToken : " + idToken);

        String jwtToken = oauth2Service.verifyGoogleToken(accessToken, idToken);
        if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("소셜 로그인 실패");
        }
        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        return ResponseEntity.ok(response);
    }
}

