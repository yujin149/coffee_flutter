package com.shop.dto;

import com.shop.constant.BoardStatus;
import com.shop.entity.Board;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardDto {
    private Long id; //게시글 번호
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private String writer; // 작성자
    private LocalDateTime regTime; // 작성일시
    private int hits; // 조회수
    private BoardStatus boardStatus; // 게시글 종류


    // Board 엔티티를 BoardDto로 변환하는 생성자
    public BoardDto(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.writer = board.getWriter();
        this.regTime = board.getRegTime();
        this.hits = board.getHits();
        this.boardStatus = board.getBoardStatus();
    }

    // BoardDto를 BoardFormDto로 변환하는 메소드
    public BoardFormDto toFormDto() {
        BoardFormDto formDto = new BoardFormDto();
        formDto.setId(this.id);
        formDto.setTitle(this.title);
        formDto.setContent(this.content);
        formDto.setWriter(this.writer);
        formDto.setBoardStatus(this.boardStatus);
        return formDto;
    }
}
