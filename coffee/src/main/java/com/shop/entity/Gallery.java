package com.shop.entity;


import com.shop.constant.GalleryStatus;
import com.shop.dto.GalleryFormDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name="gallery")
@Getter
@Setter
@ToString

public class Gallery extends BaseEntity{
    @Id
    @Column(name="gallery_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title; //제목

    @Column(nullable = false)
    private String content; //내용

    @Column(nullable = false)
    private LocalDate startTime; //이벤트 시작일

    @Column(nullable = false)
    private LocalDate endTime; //이벤트 종료일

    @Enumerated(EnumType.STRING)
    private GalleryStatus galleryStatus; //갤러리 상태

    public void updateGallery(GalleryFormDto galleryFormDto){
        this.title = galleryFormDto.getTitle();
        this.content = galleryFormDto.getContent();
        this.startTime = galleryFormDto.getStartTime();
        this.endTime = galleryFormDto.getEndTime();
        this.galleryStatus = galleryFormDto.getGalleryStatus();
    }

    
    
}
