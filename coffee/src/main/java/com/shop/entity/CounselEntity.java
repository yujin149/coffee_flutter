package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 1:1 채팅 메시지 엔티티
 * 채팅 메시지의 정보를 데이터베이스에 저장하기 위한 엔티티 클래스
 */
@Entity
@Table(name = "counsel")
@Getter @Setter 
public class CounselEntity {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가(Auto Increment) 설정
    private Long id; 

    private String username;  // 사용자 이름 (표시용)
    private String userId;    // 메시지를 받는 사용자의 ID
                             // 일반 사용자의 경우 본인 ID, 관리자의 경우 상담 대상 사용자 ID
    
    private String adminId;   // 관리자 ID (관리자가 보낸 메시지인 경우에만 사용)
    private String message;   // 실제 채팅 메시지 내용
    
    private LocalDateTime regDate;  // 메시지 등록 일시
    
    private String senderName;  // 메시지를 보낸 사람의 실제 이름
                               // 채팅방에서 표시되는 이름
    
    private String senderId;    // 메시지를 보낸 사람의 ID
                               // 발신자 구분을 위해 사용
    
    private Integer unreadCount = 0;  // 읽지 않은 메시지 수
                                     // 새 메시지 알림을 위해 사용
                                     // 기본값 0으로 초기화

}
