package com.shop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

//인증되지 않는 사용자가 리소스 요청하면 차단 하는 클래스

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    //AuthenticationException 인증예외
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
        throws IOException, ServletException{

        String requestURI = request.getRequestURI();
        System.out.println("Request URI: " + requestURI);


        if (authException instanceof InsufficientAuthenticationException) {
            if (requestURI.startsWith("/cart")) { // /cart 로 401에러
                System.out.println("비로그인 주문 요청 처리");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }

            HttpSession session = request.getSession(false);

            if (session != null) {
                if (session.getAttribute("emailVerified") == null) {
                    session.invalidate();
                }
            }

            System.out.println("여기");
            String errorMessage = "이미 등록된 이메일입니다. 다른 소셜 계정을 사용해주세요.";
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<script>alert('" + errorMessage + "'); location.href='/members/login';</script>");
        } else {
            System.out.println("aaa");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}
