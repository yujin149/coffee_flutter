package com.shop.controller;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {


    @GetMapping("/debug")
    public void debug(Authentication authentication) {
        if (authentication != null) {
            System.out.println("현재 사용 권한: " + authentication.getAuthorities());
        } else {
            System.out.println("authentication 객체가 null입니다.");
        }
    }
}
