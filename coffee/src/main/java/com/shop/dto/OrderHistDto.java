package com.shop.dto;


import com.shop.constant.OrderStatus;
import com.shop.entity.Member;
import com.shop.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderHistDto {
    private Long orderId;                        // 주문 ID
    private String orderDate;                    // 주문 날짜
    private OrderStatus orderStatus;             // 주문 상태
    private List<OrderItemDto> orderItemDtoList = new ArrayList<>(); // 주문 상품 리스트
    private Member member;
    private int usedMembership;
    private int finalPrice;

    // Order 엔티티를 기반으로 DTO 생성
    public OrderHistDto(Order order) {
        this.orderId = order.getId();
        this.orderDate = order.getOrderDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.orderStatus = order.getOrderStatus();
        this.member = order.getMember();
        this.usedMembership = order.getUsedMembership();
        this.finalPrice = order.getFinalPrice();
    }

    // 주문 상품 DTO를 리스트에 추가
    public void addOrderItemDto(OrderItemDto orderItemDto) {
        orderItemDtoList.add(orderItemDto);
    }
}
