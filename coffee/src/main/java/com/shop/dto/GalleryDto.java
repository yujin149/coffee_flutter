package com.shop.dto;



import com.shop.constant.GalleryStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter

public class GalleryDto {
    private Long id;
    private String title; //제목
    private String content; //내용
    private LocalDate startTime; //이벤트시작일
    private LocalDate endTime; //이벤트 종료일
    private String repImgUrl; // 대표 이미지 URL
    private GalleryStatus galleryStatus; // 이벤트 상태 추가
}
