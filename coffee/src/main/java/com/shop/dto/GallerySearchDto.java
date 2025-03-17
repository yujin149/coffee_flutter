package com.shop.dto;

import com.shop.constant.GalleryStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GallerySearchDto {
    private GalleryStatus galleryStatus; // 이벤트 상태
    private String searchBy; //이벤트 조회시 어떤 유형으로 조회할지
    private String searchQuery = ""; //조회할 검색어 저장할 변수
}
