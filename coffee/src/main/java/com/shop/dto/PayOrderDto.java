//package com.shop.dto;
//
//import com.shop.entity.PayOder;
//import lombok.Getter;
//
//@Getter
//public class PayOrderDto {
//    Long productId;        // 상품 ID
//    String productName;    // 상품명
//    int price;             // 가격
//    int quantity;          // 주문 수량
//    String impUid;         // 아임포트 결제 고유 ID
//    String merchantUid;    // 상점 주문 고유 번호
//
//    // PayOrder 엔티티로 변환
//    public PayOder toEntity() {
//        return PayOder.builder()
//                .productId(productId)
//                .productName(productName)
//                .price(price)
//                .quantity(quantity)
//                .impUid(impUid)
//                .merchantUid(merchantUid)
//                .build();
//    }
//}
