package com.shop.controller;


import com.shop.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;


    @GetMapping(value = "/chat")
    public String chatView() {
        return "chat/chatMain";
    }



    @PostMapping(value = "/chat", produces = "application/json")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        try {
            // 입력 데이터 검증
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "메시지를 입력해주세요."
                ));
            }

            // ChatService 호출
            String response = chatService.getAnswer(message);

            // 정상 응답 반환
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "response", response
            ));
        } catch (Exception ex) {
            // 예외 처리 및 에러 응답 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "서버 오류가 발생했습니다.",
                    "details", ex.getMessage()
            ));
        }
    }
}
