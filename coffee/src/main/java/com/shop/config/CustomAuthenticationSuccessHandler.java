package com.shop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;


//로그인 성공후 사용자 정보 출력
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Authentication authentication)throws IOException, ServletException {

        String userName = authentication.getName();


        System.out.println(userName + "님, 로그인 성공!");


        System.out.println(userName);

        httpServletResponse.setContentType("text/html;charset=UTF-8");
        httpServletResponse.getWriter().write("<script>alert('"+ userName + "님 환영합니다.'); location.href='/';</script>");
    }
}
