package com.shop.api;

import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/members/api")
@Controller
public class AuthController {
    private final JwtUtil jwtUtil;  // JWT 유틸리티 클래스 (토큰 생성, 검증)
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil, MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }




    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {



        // 1️⃣ DB에서 사용자 찾기
        Member member = memberRepository.findByUserid(request.getUserid());
        System.out.println(member);

        // 2️⃣ 회원이 없거나 비밀번호가 틀리면 실패
        if (member == null || !passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            System.out.println("member null");
            return ResponseEntity.status(403).body("로그인 실패: 아이디 또는 비밀번호가 틀립니다.");
        }

        // 3️⃣ JWT 토큰 생성
        String token = jwtUtil.generateToken(member.getUserid());

        // 4️⃣ 응답으로 JWT 토큰 반환
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        System.out.println("return 전에 token: "+token);
        return ResponseEntity.ok(response);
    }

    // 👤 [사용자 정보 가져오기] GET /members/api/info
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {

        System.out.println("api 인포폼 들어옴??");
        System.out.println("token :" + token);

        // "Bearer " 제거
        if (token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
        } else {
            return ResponseEntity.status(403).body("유효하지 않은 토큰입니다.");
        }
        System.out.println("Received Token: " + token);

        // JWT 토큰 검증
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(403).body("유효하지 않은 토큰입니다.");
        }

        // 토큰에서 아이디 추출
        String userid = jwtUtil.extractUserid(token);

        // 사용자 정보 예제 (DB 연동 필요)
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("userid", userid);
        userInfo.put("nickname", "테스트 유저");

        return ResponseEntity.ok(userInfo);
    }
}
