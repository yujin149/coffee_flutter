package com.shop.entity;

import com.shop.constant.BoardStatus;
import com.shop.dto.BoardFormDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 게시글 정보를 저장하는 엔티티 클래스
 */
@Entity                // JPA 엔티티 클래스임을 나타냄
@Table(name = "board") // 데이터베이스의 'board' 테이블과 매핑
@Getter @Setter
public class Board extends BaseEntity { // BaseEntity의 생성일, 수정일 등을 상속

    @Id // 기본키(Primary Key) 지정
    @Column(name = "board_id")
    @GeneratedValue(strategy = GenerationType.AUTO) // 자동 증가 전략 사용
    private Long id; // 게시글 번호

    @Column(nullable = false) // NOT NULL 제약조건
    private String title; // 게시글 제목

    @Column(nullable = false, columnDefinition = "TEXT") // TEXT 타입으로 지정
    private String content; // 게시글 내용

    @Column(nullable = false)
    private String writer; // 작성자

    @Column(columnDefinition = "integer default 0") // 기본값 0으로 설정
    private int hits; // 조회수

    @Enumerated(EnumType.STRING) // enum 값을 문자열로 저장
    private BoardStatus boardStatus; // 게시글 종류(공지/일반)

    /**
     * 게시글 수정 메소드
     * BoardFormDto의 데이터로 게시글 정보를 업데이트
     */
    public void updateBoard(BoardFormDto boardFormDto) {
        this.title = boardFormDto.getTitle();
        this.content = boardFormDto.getContent();
        this.boardStatus = boardFormDto.getBoardStatus();
    }
}
