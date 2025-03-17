package com.shop.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderDto {
    private Long itemId;         // 상품 ID
    private int count;           // 주문 수량
    private int totalPrice;
    private int finalPrice;
    private String imgUrl;
    private String itemNm;
    private int usedMembership;

    private String impUid;       // 결제 고유 ID (아임포트 결제 성공 시 제공)
    private String merchantUid;  // 상점 고유 주문 번호
    private int paidAmount;      // 결제된 금액



    //이것도 실험용
    private List<OrderItemDto> orderItems;
}
