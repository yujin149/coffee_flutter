package com.shop.dto;

import com.shop.constant.BoardStatus;
import com.shop.entity.Board;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//게시글 작성 및 수정 폼에서 사용되는 DTO 클래스
public class BoardFormDto {
    private Long id; // 게시글 번호 (수정 시에만 사용)

    // 게시글 제목 (필수 입력값)
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title; 

    // 게시글 내용 (필수 입력값)
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;

    
    private String writer; // 작성자
    private BoardStatus boardStatus; // 게시글 종류 (공지/일반)

    // BoardFormDto를 Board 엔티티로 변환하는 메소드
    public Board createBoard(String writer) {
        Board board = new Board();
        board.setTitle(this.title);
        board.setContent(this.content);
        board.setWriter(writer);
        board.setBoardStatus(this.boardStatus);
        return board;
    }
}
