package com.shop.config;

import com.shop.service.VisitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class VisitInterceptor implements HandlerInterceptor {
    
    private final VisitService visitService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isResourceRequest(request) || isAjaxRequest(request)) {
            return true;
        }

        HttpSession session = request.getSession();

        Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");


        System.out.println("emailVerified : " + emailVerified);
        System.out.println("verifiedEmail : " + verifiedEmail);



        String sessionKey = "visited_" + LocalDate.now().toString();

        if (session.getAttribute(sessionKey) == null) {
            visitService.recordVisit(request);
            session.setAttribute(sessionKey, true);
        }

        if (emailVerified != null) {
            session.setAttribute("emailVerified", emailVerified);
        }
        if (verifiedEmail != null) {
            session.setAttribute("verifiedEmail", verifiedEmail);
        }

        return true;
    }

    private boolean isResourceRequest(HttpServletRequest request) {
        String path = request.getRequestURI().toLowerCase();
        return path.contains(".") ||  // 모든 파일 확장자 요청 제외
            path.startsWith("/api/") || 
            path.startsWith("/admin/") ||
            path.startsWith("/resources/") ||
            path.startsWith("/static/") ||
            path.startsWith("/images/") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/login") ||
            path.startsWith("/members/login") ||
            path.contains("login") ||
            path.contains("logout");
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        String contentType = request.getHeader("Content-Type");
        String referer = request.getHeader("Referer");
        
        return (requestedWith != null && requestedWith.equals("XMLHttpRequest")) ||
            (accept != null && accept.contains("application/json")) ||
            (contentType != null && contentType.contains("application/json")) ||
            (referer != null && (referer.contains("login") || referer.contains("logout"))) ||
            request.getRequestURI().contains("/api/");
    }
} 