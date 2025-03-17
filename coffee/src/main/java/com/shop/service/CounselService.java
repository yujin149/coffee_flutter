package com.shop.service;

import com.shop.entity.CounselEntity;
import com.shop.repository.CounselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 채팅 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 채팅 메시지의 저장, 조회, 읽음 처리 등을 담당
 */
@Service
@RequiredArgsConstructor
public class CounselService {
    private final CounselRepository counselRepository;

    /**
     * 관리자용 전체 채팅 목록 조회
     * 각 사용자별로 가장 최근 메시지만 조회하여 채팅 목록을 구성
     * 
     * @return 사용자별 최신 메시지 목록 (최근 메시지 순으로 정렬)
     */
    @Transactional(readOnly = true)
    public List<CounselEntity> getAllCounselList() {
        // 모든 메시지 조회 (counsel 테이블에서 모든 메세지를 가져옴)
        List<CounselEntity> allMessages = counselRepository.findAll();
        //HashMap 사용하여 각 사용자의 최신 메세지만 저장
        //key: 사용자 ID, value: 최신 메세지
        Map<String, CounselEntity> latestMessages = new HashMap<>(); 
        
        // 각 사용자별로 가장 최근 메시지만 선택
        for (CounselEntity message : allMessages) {
            String userId = message.getUserId();
            if (userId != null && !userId.equals("admin")) { //관리자가 아닌 사용자의 메세지만 처리
                CounselEntity existing = latestMessages.get(userId); //HashMap에서 해당 사용자의 최신 메세지를 가져옴
                if (existing == null || existing.getRegDate().isBefore(message.getRegDate())) {
                    //처음이면 null이 반환됨.
                    //이전 메세지가 있었다면 새로운 메세지를 덮어씌움.
                    latestMessages.put(userId, message); //각 사용자별로 가장 최근 메세지만 저장
                }
            }
        }
        
        // 최근 메시지 순으로 정렬하여 반환
        //HashMap의 values() 메서드를 사용하여 모든 메세지를 가져온 후, 등록일시(regDate)를 기준으로 내림차순 정렬

        //latestMessages.values() : 모든 메세지를 가져옴.
        //stream() : 스트림 생성 (데이터를 순차적으로 처리하기 위한 방식)
        //sorted((m1, m2) -> m2.getRegDate().compareTo(m1.getRegDate())) : 등록일시(regDate)를 기준으로 내림차순 정렬
        //collect(Collectors.toList()) : 정렬된 메세지를 리스트로 반환
        return latestMessages.values().stream()
                .sorted((m1, m2) -> m2.getRegDate().compareTo(m1.getRegDate()))
                .collect(Collectors.toList());
    }

    /**
     * 새로운 채팅 메시지 저장
     * 메시지 등록 시간 설정 및 읽지 않은 메시지 수 처리
     * 
     * @param counselEntity 저장할 채팅 메시지 정보
     */
    public void saveCounsel(CounselEntity counselEntity) {
        // 등록 시간이 없는 경우 현재 시간으로 설정
        if (counselEntity.getRegDate() == null) {
            counselEntity.setRegDate(LocalDateTime.now());
        }
        // 사용자가 보낸 메시지인 경우 읽지 않은 메시지 수 증가
        // 사용자가 보낸 메시지인 경우에만 unreadCount를 1로 설정
        if (counselEntity.getUserId() != null && !counselEntity.getUserId().equals("admin")) {
            counselEntity.setUnreadCount(1); // 새 메시지는 '안읽음' 상태
        }
        counselRepository.save(counselEntity);
    }

    /**
     * 메시지 읽음 처리
     * 특정 사용자의 모든 메시지를 읽음 상태로 변경
     * 
     * @param userId 읽음 처리할 사용자 ID
     */
    @Transactional
    public void markAsRead(String userId) {
        List<CounselEntity> messages = counselRepository.findByUserIdOrderByRegDateAsc(userId);
        for (CounselEntity message : messages) {
            message.setUnreadCount(0);
            counselRepository.save(message);
        }
    }

    /**
     * 특정 사용자의 읽지 않은 메시지 수 조회
     * 
     * @param userId 조회할 사용자 ID
     * @return 읽지 않은 메시지 수
     */
    public int getUnreadMessageCount(String userId) {
        List<CounselEntity> messages = counselRepository.findByUserIdOrderByRegDateAsc(userId);
        return messages.stream()
                .mapToInt(message -> message.getUnreadCount() != null ? message.getUnreadCount() : 0)
                .sum();
    }

    /**
     * 특정 사용자의 채팅 내역 조회
     * 시간순으로 정렬된 모든 메시지 반환
     * 
     * @param userId 조회할 사용자 ID
     * @param adminId 관리자 ID (미사용)
     * @return 채팅 메시지 목록
     */
    public List<CounselEntity> getChatHistory(String userId, String adminId) {
        return counselRepository.findByUserIdOrderByRegDateAsc(userId);
    }

    /**
     * 특정 사용자의 메시지 목록 조회 (디버그 로그 포함)
     * 조회 과정의 상세 로그를 출력하며 에러 처리
     * 
     * @param userId 조회할 사용자 ID
     * @return 채팅 메시지 목록
     */
    @Transactional(readOnly = true)
    public List<CounselEntity> findMessagesByUserId(String userId) {
        try {
            System.out.println("=== Finding Messages for User ===");
            System.out.println("User ID: " + userId);
            
            //userId가 null이거나 비어있는 경우 예외 처리
            //사용자 Id 유효성 검사
            if (userId == null || userId.trim().isEmpty()) {
                System.out.println("Invalid userId provided");
                return Collections.emptyList();
            }

            //userId에 해당하는 모든 메세지를 조회
            //특정 사용자의 모든 채팅 메세지를 시간순으로 조회
            List<CounselEntity> messages = counselRepository.findByUserIdOrderByRegDateAsc(userId);
            
            // 디버그 로깅
            //조회된 메세지가 null인 경우 예외 처리
            if (messages == null) {
                System.out.println("Repository returned null for userId: " + userId);
                return Collections.emptyList();
            }

            //조회된 메세지가 있는 경우 디버그 로깅
            System.out.println("Found " + messages.size() + " messages for userId: " + userId);
            
            //메세지가 비어있지 않은 경우 디버그 로깅
            if (!messages.isEmpty()) {
                System.out.println("Message details:");
                for (CounselEntity message : messages) {
                    //각 메세지의 상세 정보를 출력
                    System.out.println("  ID: " + message.getId()); //메세지의 고유 식별자
                    System.out.println("  Sender ID: " + message.getSenderId()); //메세지 보낸 사람의 ID
                    System.out.println("  User ID: " + message.getUserId()); //메세지 받은 사람의 ID
                    System.out.println("  Message: " + message.getMessage()); //메세지 내용
                    System.out.println("  Date: " + message.getRegDate()); //메세지 전송시간
                }
            }

            //조회된 메세지를 반환
            return messages;    
        } catch (Exception e) {
            //예외 발생 시 예외 정보를 출력하고 빈 리스트 반환
            System.out.println("=== Error in findMessagesByUserId ==="); //예외 발생 시 예외 정보를 출력하고 빈 리스트 반환 
            System.out.println("Error type: " + e.getClass().getName()); //예외 타입
            System.out.println("Error message: " + e.getMessage()); //예외 메세지
            e.printStackTrace(); //예외 발생 시 예외 정보를 출력
            return Collections.emptyList(); //빈 리스트 반환
        }
    }

    /**
     * 읽지 않은 메시지 수 초기화
     * 채팅방 입장 시 호출되어 모든 메시지를 읽음 상태로 변경
     * 
     * @param userId 초기화할 사용자 ID
     */
    public void resetUnreadCount(String userId) {
        //userId에 해당하는 모든 메세지를 조회 (등록시간순 정렬)
        List<CounselEntity> messages = counselRepository.findByUserIdOrderByRegDateAsc(userId);
        for (CounselEntity message : messages) {
            // 메세지를 '읽음'상태로 표시
            // 새 메세지가 도착하면 unreadCount = 1(안읽은 상태)
            // 메세지 읽음 -> unreadCount = 0 (읽음 상태)
            // 한번 0으로 설정된 메세지는 다시 1로 변경되지 않음.
            message.setUnreadCount(0); //읽지 않은 메세지 수를 0으로 설정 (채팅방 입장 시)
            counselRepository.save(message); //변경된 메세지를 저장
        }
    }


    /**
     * 관리자용 전체 읽지 않은 메시지 수 조회
     * 모든 사용자의 읽지 않은 메시지 수 합계 반환
     * 
     * @return 전체 읽지 않은 메시지 수
     */
    public int getTotalUnreadMessageCountForAdmin() {
        //모든 메세지를 조회
        List<CounselEntity> allMessages = counselRepository.findAll();
        //읽지 않은 메세지 수 계산
        return allMessages.stream()
                //관리자가 보낸 메세지는 제외 (사용자가 보낸 메세지만 필터링)
                .filter(message -> !message.getSenderId().equals("admin"))
                //각 메세지의 읽지 않은 수를 정수로 변환 (unreadCount를 숫자로 변환 / null인 경우 0으로 변환)
                .mapToInt(message -> message.getUnreadCount() != null ? message.getUnreadCount() : 0)
                // 모든 값을 합산
                .sum();
    }

    /**
     * 일반 사용자용 읽지 않은 메시지 수 조회
     * 관리자가 보낸 읽지 않은 메시지 수 반환
     * 
     * @param userId 조회할 사용자 ID
     * @return 읽지 않은 메시지 수
     */
    public int getUnreadMessageCountForUser(String userId) {
        //userId에 해당하는 모든 메세지를 조회 (등록시간순 정렬)
        List<CounselEntity> messages = counselRepository.findByUserIdOrderByRegDateAsc(userId);
        return messages.stream()
                //관리자가 보낸 메세지만 필터링 
                .filter(message -> "관리자".equals(message.getUsername()))
                //각 메세지의 읽지 않은 수를 정수로 변환 (unreadCount를 숫자로 변환 / null인 경우 0으로 변환)
                .mapToInt(message -> message.getUnreadCount() != null ? message.getUnreadCount() : 0)
                .sum();
    }
}
