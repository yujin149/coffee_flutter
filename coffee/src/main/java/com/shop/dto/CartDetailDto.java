package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 장바구니 상세 정보를 담는 DTO
 */
@Getter
@Setter
public class CartDetailDto {
    private Long cartItemId;  // 장바구니 항목 ID
    private String itemNm;    // 상품명
    private int price;        // 상품 가격
    private int count;        // 상품 수량
    private String imgUrl;    // 상품 이미지 URL

    // 생성자
    public CartDetailDto(Long cartItemId, String itemNm, int price, int count, String imgUrl) {
        this.cartItemId = cartItemId;
        this.itemNm = itemNm;
        this.price = price;
        this.imgUrl = imgUrl;
        this.count = count;
    }
}
