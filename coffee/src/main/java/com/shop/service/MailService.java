package com.shop.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "leminus526@gmail.com";

    // 이메일 주소와 인증코드를 저장 하는 Map
    private final Map<String, String> authCodestorage = new HashMap<>();

    // 랜덤으로 숫자 생성

    public String createNumber() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) { // 인증 코드 8자리
            int index = random.nextInt(3); // 0~2까지 랜덤, 랜덤값으로 switch문 실행

            switch (index) {
                case 0 -> key.append((char) (random.nextInt(26)+97)); //소문자
                case 1 -> key.append((char) (random.nextInt(26) + 65)); // 대문자
                case 2 -> key.append(random.nextInt(10)); // 숫자

            }
        }
        return key.toString();
    }

    // 이메일 인증 메일 생성
    public MimeMessage createMail(String mail, String number) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, mail);
        message.setSubject("이메일 인증");
        String body = "";
        body += "<h3>요청하신 인증 번호입니다.</h3>";
        body += "<h1>" + number + "</h1>";
        body += "<h3>감사합니다.</h3>";
        message.setText(body, "UTF-8", "html");

        return message;

    }

    // 이메일 인증 요청
    public void sendSimpleMessage(String email) throws MessagingException {
        String authCode = createNumber(); // 인증 코드 생성
        System.out.println("생성된 인증 코드: " + authCode);
        authCodestorage.put(email, authCode); // 인증 코드 저장

        MimeMessage message = createMail(email, authCode);
        System.out.println(message+"*************");
        try {
            System.out.println(message+"sdfijsadifjsdiajfiajdsijfi");
            javaMailSender.send(message); // 메일 발송
            System.out.println("메일 발송 준비"+ message);
        } catch (MailException e) {
            // 메일 발송 실패 시 더 자세한 로그를 출력합니다.
            System.out.println("메일 실패"+ message);
            e.printStackTrace();
            System.err.println("MailException 원인: " + e.getMessage());  // 에러 메시지 출력
            Throwable cause = e.getCause();
            if (cause != null) {
                // 예외 원인이 AuthenticationFailedException일 때
                if (cause instanceof jakarta.mail.AuthenticationFailedException) {
                    System.err.println("인증 실패: SMTP 서버에 로그인할 수 없습니다.");
                }
                // 예외 원인이 MessagingException일 때
                else if (cause instanceof MessagingException) {
                    System.err.println("메시지 전송 오류");
                } else {
                    System.err.println("기타 오류: " + cause.getClass().getName());
                }
            }
            throw new IllegalArgumentException("메일 발송 중 오류가 발생했습니다.");
        }
    }

    // 인증 코드 확인
    public boolean verifyAuthCode(String email, String inputCode) {
        String storedCode = authCodestorage.get(email); // 저장된 인증 코드 조회
        return storedCode != null && storedCode.equals(inputCode); // 인증 코드 검증
    }
}
