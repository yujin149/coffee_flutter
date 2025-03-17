package com.shop.controller;


import com.shop.constant.LoginType;
import com.shop.dto.MemberFormDto;
import com.shop.dto.ValidationGroups;
import com.shop.service.MemberService;
import com.shop.utill.MemberPasswordCk;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MypageController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final MemberPasswordCk memberPasswordCk;


    @GetMapping(value = "/mypage")
    public String myPage(Principal principal, Model model) {
        model.addAttribute(principal.getName());
        if (principal.getName() == null) {
            return "member/memberLoginForm";
        }
        return "mypage/mypageMain";
    }


    @GetMapping(value = "/mypage/update")
    @ResponseBody
//    public MemberFormDto updateDtl(Principal principal) {
    public ResponseEntity<Object> updateDtl(Principal principal) {

        try {

            System.out.println("Principal Name: " + principal.getName());

            MemberFormDto memberFormDto = memberService.getMemberDtl(principal.getName());
//            if ("SOCIAL".equals(memberFormDto.getLoginType())) {
//                System.out.println("간편로그인 실패 확인");
//                return ResponseEntity.badRequest().body(Map.of("error", "간편로그인은 정보수정이 불가능합니다."));
//            }
            return ResponseEntity.ok(memberFormDto);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 사용자입니다."));
        }
    }


    @PostMapping(value = "/mypage/update")
    public ResponseEntity mypageUpdate(@Validated(ValidationGroups.Update.class) MemberFormDto memberFormDto, BindingResult bindingResult,
                               Model model,@RequestHeader(name = "X-CSRF-TOKEN", required = false) String csrfToken) {

        System.out.println("CSRF 토큰: " + csrfToken);
        System.out.println("회원 수정 요청 도착 id: " + memberFormDto.getUserid());
        System.out.println("회원 수정 요청 도착 email: " + memberFormDto.getEmail());
        if (bindingResult.hasErrors()) {



            memberFormDto.setPostcode(""); // 우편번호 초기화
            memberFormDto.setAddress(""); // 주소 초기화

            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> {
                errorMessage.append(error.getDefaultMessage()).append("\n");
            });
            System.out.println("유효성 검사 실패: " + bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(errorMessage.toString().trim());
        }
        try {
            System.out.println("회원 정보 수정 성공");
            memberService.memberUpdate(memberFormDto);
            return ResponseEntity.ok().body("회원 정보 수정 성공");
        } catch (Exception e) {
            System.err.println("회원 정보 수정 중 에러: " + e.getMessage());
            model.addAttribute("errorMessage","수정 중 에러 발생");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"회원 정보 수정 중 에러 발생\"}");
        }

    }
    @GetMapping(value = "/mypage/password")
    @ResponseBody
    public ResponseEntity<Object> passwordDtl(Principal principal){
        try {

            System.out.println("Principal Name: " + principal.getName());

            MemberFormDto memberFormDto = memberService.getMemberDtl(principal.getName());
            if ("SOCIAL".equals(memberFormDto.getLoginType())) {
                System.out.println("간편로그인 실패 확인");
                return ResponseEntity.badRequest().body(Map.of("error", "간편로그인은 비밀번호 변경이 불가능합니다."));
            }
            return ResponseEntity.ok(memberFormDto);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 사용자입니다."));
        }

    }



    @PostMapping(value = "/mypage/password")
    public ResponseEntity mypagePassword(@Validated(ValidationGroups.Pwd.class)MemberFormDto memberFormDto,
                                         BindingResult bindingResult, Model model, HttpSession session,
                                         @RequestHeader(name = "X-CSRF-TOKEN",required = false)String token){

        try {

            System.out.println("Received MemberFormDto: " + memberFormDto);
            memberPasswordCk.validate(memberFormDto, bindingResult);

            // 이메일 인증 체크
            Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
            if (emailVerified == null || !emailVerified) {
                bindingResult.rejectValue("email", "invalidEmail", "이메일 인증을 완료해야 합니다.");
                return ResponseEntity.badRequest().body("이메일 인증을 완료해야 합니다.");
            }

            // 유효성 검사 실패 처리
            if (bindingResult.hasErrors()) {
                StringBuilder errorMessage = new StringBuilder();
                bindingResult.getAllErrors().forEach(error -> {
                    errorMessage.append(error.getDefaultMessage()).append("\n");
                });
                System.out.println("유효성 검사 실패: " + bindingResult.getAllErrors());
                return ResponseEntity.badRequest().body(errorMessage.toString().trim());
            }

            // 비밀번호 업데이트
            memberService.memberUpdate(memberFormDto);
            System.out.println("비밀번호 수정 성공");
            return ResponseEntity.ok().body("비밀번호 수정 성공");

        } catch (EntityNotFoundException e) {
            System.out.println("회원 정보 없음: " + memberFormDto.getUserid());
            return ResponseEntity.badRequest().body("{\"error\": \"존재하지 않는 사용자입니다.\"}");
        } catch (Exception e) {
            System.out.println("회원 이름: "+ memberFormDto.getName());
            System.err.println("회원 정보 수정 중 에러: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"회원 정보 수정 중 에러 발생\"}");
        }
    }
}
