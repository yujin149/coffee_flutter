package com.shop.repository;

import com.shop.entity.CounselEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselRepository extends JpaRepository<CounselEntity, Long> {
    
    /**
     * 특정 사용자의 채팅 메시지 목록을 조회
     * 
     * findBy: 검색 시작
     * UserId: userId 필드를 기준으로 검색
     * OrderByRegDateAsc: 등록일시(regDate)를 기준으로 오름차순 정렬
     * 
     * 실제 실행되는 SQL:
     * SELECT * FROM counsel 
     * WHERE user_id = ? 
     * ORDER BY reg_date ASC
     * 
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 채팅 메시지 목록 (시간순 정렬)
     */
    List<CounselEntity> findByUserIdOrderByRegDateAsc(String userId);
}
