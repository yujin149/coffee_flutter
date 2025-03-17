package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InquiryDto {
    private Long id;
    private String title; //제목
    private String content; //내용
    private String writer; //작성자
    private LocalDateTime regTime; // 작성일시
    private boolean secret; //비밀글 설정

    private InquiryDto inquiryDto; //문의 상태



}
