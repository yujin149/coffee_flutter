package com.shop.controller;

import com.shop.entity.CounselEntity;
import com.shop.entity.Member;
import com.shop.service.CounselService;
import com.shop.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 1:1 채팅 기능을 위한 컨트롤러
 * 채팅 목록, 채팅방, 메시지 전송 등의 기능을 처리
 */
@Controller
@RequiredArgsConstructor
public class CounselController {

    private final CounselService counselService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송을 위한 유틸리티
    private final MemberService memberService;

    /**
     * 관리자용 채팅 목록 페이지
     * 모든 사용자와의 채팅 내역을 목록으로 표시
     * 
     * @param model 뷰에 전달할 데이터를 담는 객체
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 채팅 목록 페이지 뷰
     */
    @GetMapping(value = "/counsel/list")
    public String counselList(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails != null && 
                         userDetails.getAuthorities().stream()
                         .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            return "redirect:/"; // 관리자가 아닌 경우 메인 페이지로 리다이렉트
        }

        // 모든 채팅 메시지 조회
        List<CounselEntity> counselList = counselService.getAllCounselList();
        
        // 각 채팅 메시지의 사용자 정보 가져오기
        for (CounselEntity counsel : counselList) {
            if (counsel.getUserId() != null) {
                try {
                    Member member = memberService.findMemberByUserid(counsel.getUserId());
                    counsel.setSenderName(member.getName());
                    counsel.setSenderId(member.getUserid());
                    // 읽지 않은 메시지 수 설정
                    counsel.setUnreadCount(counselService.getUnreadMessageCount(counsel.getUserId()));
                } catch (EntityNotFoundException e) {
                    counsel.setSenderName("알 수 없음");
                    counsel.setSenderId(counsel.getUserId());
                    counsel.setUnreadCount(0);
                }
            }
        }
        
        model.addAttribute("counselList", counselList);
        return "counsel/counselList";
    }

    /**
     * 채팅방 페이지
     * 관리자와 사용자 간의 1:1 채팅이 이루어지는 페이지
     * 
     * @param userId 채팅 상대방 ID (관리자가 접근할 때 필요)
     * @param model 뷰에 전달할 데이터를 담는 객체
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 채팅방 페이지 뷰
     */

    //일반로그인
    @GetMapping(value = "/counsel")
    public String counsel(@RequestParam(required = false) String userId, Model model, 
    @AuthenticationPrincipal UserDetails userDetails) {
        // 현재 로그인한 사용자가 관리자인지 확인
        boolean isAdmin = userDetails != null && 
                         userDetails.getAuthorities().stream()
                         .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // 현재 로그인한 사용자의 정보 가져오기
        Member currentMember = memberService.findMemberByUserid(userDetails.getUsername());
        model.addAttribute("userName", currentMember.getName());
        model.addAttribute("currentUserId", currentMember.getUserid());
        
        // 채팅 상대방의 정보 가져오기 (userId가 있는 경우)
        if (userId != null) {
            Member chatPartner = memberService.findMemberByUserid(userId);
            model.addAttribute("partnerName", chatPartner.getName());
            model.addAttribute("partnerId", chatPartner.getUserid());
            
            // 관리자가 채팅방에 들어올 때 해당 사용자의 메시지를 읽음 처리
            if (isAdmin) {
                counselService.markAsRead(userId);
            }
        }
        
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("userId", userId);
        return "counsel/counsel";
    }
    //소셜로그인 추가 OAuth2User 여기에 소셜로그인 정보가있음
    @GetMapping(value = "/counselSocial")
    public String counselSocail(@RequestParam(required = false) String userId, Model model,
                          @AuthenticationPrincipal OAuth2User userDetails) {
        // 현재 로그인한 사용자가 관리자인지 확인
        boolean isAdmin = userDetails != null &&
                userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 현재 로그인한 사용자의 정보 가져오기
        Member currentMember = memberService.findMemberByUserid(userDetails.getName());
        model.addAttribute("userName", currentMember.getName());
        model.addAttribute("currentUserId", currentMember.getUserid());

        // 채팅 상대방의 정보 가져오기 (userId가 있는 경우)
        if (userId != null) {
            Member chatPartner = memberService.findMemberByUserid(userId);
            model.addAttribute("partnerName", chatPartner.getName());
            model.addAttribute("partnerId", chatPartner.getUserid());

            // 관리자가 채팅방에 들어올 때 해당 사용자의 메시지를 읽음 처리
            if (isAdmin) {
                counselService.markAsRead(userId);
            }
        }

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("userId", userId);
        return "counsel/counsel";
    }

    /**
     * 채팅 내역 조회 API
     * 이전 채팅 내역을 불러오는 REST API
     * 
     * @param userId 채팅 내역을 조회할 사용자 ID
     * @return 채팅 내역 목록 또는 에러 응답
     */
    @GetMapping("/api/counsel/messages/{userId}")
    @ResponseBody
    public ResponseEntity<?> getChatHistory(@PathVariable String userId) {
        try {
            // 1. 인증 확인
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                System.out.println("No valid authentication found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }

            String currentUserId = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            System.out.println("=== Chat History Request ===");
            System.out.println("Requested User ID: " + userId);
            System.out.println("Current User ID: " + currentUserId);
            System.out.println("Is Admin: " + isAdmin);

            // 2. 권한 확인
            if (!isAdmin && !currentUserId.equals(userId)) {
                System.out.println("Unauthorized access attempt");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            // 3. 메시지 조회
            List<CounselEntity> messages = counselService.findMessagesByUserId(userId);
            
            if (messages == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // 4. 발신자 정보 설정
            for (CounselEntity message : messages) {
                try {
                    String senderId = message.getSenderId();
                    if (senderId == null) {
                        message.setSenderName("알 수 없음");
                        continue;
                    }

                    if ("admin".equals(senderId)) {
                        message.setSenderName("관리자");
                    } else {
                        try {
                            Member sender = memberService.findMemberByUserid(senderId);
                            if (sender != null) {
                                message.setSenderName(sender.getName());
                            } else {
                                message.setSenderName("알 수 없음");
                            }
                        } catch (Exception e) {
                            System.out.println("Error finding sender: " + e.getMessage());
                            message.setSenderName("알 수 없음");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing message: " + e.getMessage());
                    message.setSenderName("알 수 없음");
                }
            }

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.out.println("=== Error in getChatHistory ===");
            System.out.println("Error type: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving chat history: " + e.getMessage());
        }
    }

    /**
     * WebSocket 메시지 처리
     * 클라이언트가 전송한 메시지를 처리하고 해당하는 수신자에게 전달
     * 
     * @param counselEntity 전송된 채팅 메시지 정보
     */
    @MessageMapping("/counsel")
    public void sendMessage(CounselEntity counselEntity) {
        // 메시지 저장
        counselService.saveCounsel(counselEntity);

        // 개별 사용자의 채팅방으로 메시지 전송
        // /topic/user/{userId}로 구독중인 클라이언트에게 메시지 전달
        messagingTemplate.convertAndSend("/topic/user/" + counselEntity.getUserId(), counselEntity);
        
        // 관리자에게도 메시지 전송
        // /topic/admin을 구독중인 관리자 클라이언트에게 메시지 전달
        messagingTemplate.convertAndSend("/topic/admin", counselEntity);
    }

    /**
     * 읽지 않은 메시지 수 초기화 API
     * 사용자가 채팅방에 입장할 때 호출되어 읽지 않은 메시지 수를 0으로 초기화
     */
    @PostMapping("/api/counsel/reset-unread/{userId}")
    @ResponseBody
    public ResponseEntity<Void> resetUnreadCount(@PathVariable String userId) {
        try {
            // 읽지 않은 메시지 수 초기화
            counselService.resetUnreadCount(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 관리자용 읽지 않은 메시지 수 조회 API
     * 모든 사용자의 읽지 않은 메시지 총합을 반환
     */
    @GetMapping("/api/counsel/unread-count/admin")
    @ResponseBody
    public ResponseEntity<Integer> getUnreadCountForAdmin(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || !userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(counselService.getTotalUnreadMessageCountForAdmin());
    }

    //layout1.html에서 안읽은 메세지 수 표시
    @GetMapping("/api/counsel/unread-count/user/{userId}")
    @ResponseBody
    public ResponseEntity<Integer> getUnreadCountForUser(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || !userDetails.getUsername().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(counselService.getUnreadMessageCountForUser(userId));
    }
}
