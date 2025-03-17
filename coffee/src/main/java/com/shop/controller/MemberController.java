package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.dto.ValidationGroups;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import com.shop.service.MailService;
import com.shop.service.MemberService;
import com.shop.utill.MemberPasswordCk;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final MemberPasswordCk memberPasswordCk;
    private final MemberRepository memberRepository;

    @GetMapping(value = "/new")
    public String memberForm(Model model) {
        model.addAttribute("memberFormDto",new MemberFormDto());
        return "member/memberForm";
    }



    @PostMapping(value = "/new")
    public String memberForm(@Validated(ValidationGroups.SignUp.class) MemberFormDto memberFormDto, BindingResult bindingResult,
                             Model model, HttpSession session) {

        memberPasswordCk.validate(memberFormDto, bindingResult);
        // 이메일 인증 체크
        Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
        System.out.println("세션 값 emailVerified: " + session.getAttribute("emailVerified"));
        System.out.println(emailVerified);
        if (emailVerified == null || !emailVerified) {
            System.out.println(hashCode()+"유효성검사"+memberFormDto.getEmail()+"1");
            bindingResult.rejectValue("email", "invalidEmail", "이메일 인증을 완료해야 합니다.");
        }

        //@Valid 붙은 객체를 검사해서 결과에 에러가 있으면 실행
        //유효성검사 (Valid가 유효성검사를 함)
        if (bindingResult.hasErrors()) {

            //카카오 우편번호
            memberFormDto.setPostcode(""); // 우편번호 초기화
            memberFormDto.setAddress(""); // 주소 초기화

            System.out.println(hashCode()+"유효성검사"+memberFormDto.getEmail()+"2");
            return "member/memberForm";//다시 회원가입으로 돌려보냅니다. GET
        }
        if (memberRepository.existsByName(memberFormDto.getName())) {
            bindingResult.rejectValue("name", "duplicate", "이미 사용 중인 이름입니다.");
            return "member/memberForm";
        }

        if (memberRepository.existsByEmail(memberFormDto.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "이미 사용 중인 이메일입니다.");
            return "member/memberForm";
        }
        try {
            //Member 객체 생성
            Member member = Member.createMember(memberFormDto, passwordEncoder,memberRepository);
            System.out.println(memberFormDto.getUserid()+" q");
            //데이터베이스에 저장
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage",e.getMessage());
            return "member/memberForm"; //다시 회원가입으로 돌려보냅니다. GET
        }


        return "member/memberLoginForm"; //루트 경로로 리다이렉트(Spring mvc에서 리다이렉트 응답)
    }


    @GetMapping(value = "/login")
    public String loginMember(){
        return "member/memberLoginForm";
    }

    @GetMapping(value = "/login/error")
    public String loginError(Model model) {
        model.addAttribute("loginErrorMsg","아이디 또는 비밀번호를 입력해주세요");
        System.out.println();
        return "member/memberLoginForm";
    }
    @GetMapping(value = "/find")
    private String findMember(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberFind";
    }

    //아이디 찾기
    @PostMapping(value = "/findId")
    public @ResponseBody ResponseEntity<?> findIdC(@RequestBody Map<String, String> requestData) {

        String email = requestData.get("email");

        String userId = memberService.findId(email);

        if (userId != null) {
            return ResponseEntity.ok(Map.of("success", true, "id", userId));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "찾는 아이디가 없습니다."));
        }
    }


    @PostMapping(value = "/password")
    public String findPasswrdC(@Validated(ValidationGroups.Pwd.class) MemberFormDto memberFormDto, BindingResult bindingResult,
                               Model model, HttpSession session) {
        memberPasswordCk.validate(memberFormDto, bindingResult);
        // 이메일 인증 체크
        Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
        if (emailVerified == null || !emailVerified) {
            bindingResult.rejectValue("email", "invalidEmail", "이메일 인증을 완료해야 합니다.");
        }

        if (bindingResult.hasErrors()) {

            return "member/memberFind";//다시 회원가입으로 돌려보냅니다. GET
        }
        if (!memberRepository.existsByEmail(memberFormDto.getEmail())) {
            bindingResult.rejectValue("userid", "duplicate", "등록 된 이메일이 아닙니다.");
            return "member/memberFind";
        }
        if (!memberRepository.existsByUserid(memberFormDto.getUserid())) {
            bindingResult.rejectValue("email", "duplicate", "등록 된 ID가 아닙니다.");
            return "member/memberFind";
        }

        try {
            System.out.println("회원 정보 수정 성공");
            memberService.memberUpdate(memberFormDto);
        } catch (Exception e) {
            System.err.println("회원 정보 수정 중 에러: " + e.getMessage());
            model.addAttribute("errorMessage","수정 중 에러 발생");
            return "member/memberFind";
        }

        return "member/memberLoginForm";
    }



    //이메일 발송
    @PostMapping("/sendEmail")
    @ResponseBody
    public String sendEmail(@RequestParam String email) {
        System.out.println(email);
        try {
            mailService.sendSimpleMessage(email);
            System.out.println(email);
            return "인증 메일이 발송되었습니다.";
        } catch (MessagingException e) {
            System.out.println(e);
            return "메일 전송 중 오류가 발생했습니다.";
        }
    }
    //인증번호 체크
    @PostMapping("/checkCode")
    @ResponseBody
    public String checkEmailCode(@RequestParam String email,
                                 @RequestParam String emailCode,
                                 HttpSession session) {
        boolean isVerified = mailService.verifyAuthCode(email, emailCode);
        if (isVerified) {
            session.setAttribute("emailVerified", true); // 세션에 인증 완료 상태 저장
            return "success"; // JSON 응답으로 'success' 반환
        } else {
            return "fail"; // 인증 실패 시 'fail' 반환
        }
    }

}
