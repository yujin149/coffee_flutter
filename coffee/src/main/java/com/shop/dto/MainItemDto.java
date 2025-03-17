package com.shop.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainItemDto {
    private Long id;              // 상품 ID
    private String itemNm;        // 상품명
    private String itemDetail;    // 상품 상세 정보
    private String imgUrl;        // 상품 이미지 URL
    private Integer price;        // 상품 가격

    @QueryProjection
    // Querydsl 결과 조회 시 MainItemDto 객체로 바로 오도록 활용
    public MainItemDto(Long id, String itemNm, String itemDetail, String imgUrl, Integer price){
        this.id = id;
        this.itemNm = itemNm;
        this.itemDetail = itemDetail;
        this.imgUrl = imgUrl;
        this.price = price;
    }
}
