package com.shop.entity;

import com.shop.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
@Getter
@Setter
public class Order extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;
    private LocalDateTime orderDate;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
            orphanRemoval = true,fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();



    private String impUid;       // 결제 고유 ID (아임포트에서 제공)
    private String merchantUid;  // 상점 주문 번호
    private int paidAmount;      // 결제된 총 금액
    private int usedMembership; //사용한 적립금
    private int finalPrice;       //최종 결제금액
    private int goMembership;
//    private boolean refunded; // 환불 여부 필드 추가


    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    public static Order createOrder(Member member,List<OrderItem> orderItemList, int usedMembership, int finalPrice, int goMembership ){
        Order order = new Order();
        order.setMember(member);
        for(OrderItem orderItem : orderItemList){
            order.addOrderItem(orderItem);
        }
        order.setOrderStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        order.setUsedMembership(usedMembership);  // 적립금 사용 금액 설정
        order.setFinalPrice(finalPrice);
        order.setGoMembership(goMembership);
        return order;
    }

    public static Order createOrder(Member member,List<OrderItem> orderItemList, int usedMembership, int finalPrice, int goMembership ,String impUid  ,String merchantUid){
        Order order = new Order();
        order.setMember(member);
        for(OrderItem orderItem : orderItemList){
            order.addOrderItem(orderItem);
        }
        order.setOrderStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        order.setUsedMembership(usedMembership);  // 적립금 사용 금액 설정
        order.setFinalPrice(finalPrice);
        order.setGoMembership(goMembership);
        order.setImpUid(impUid);
        order.setMerchantUid(merchantUid);
        return order;
    }
    // 주문서에 있는 주문 아이템 리스트를 반복
    // 주문 아이템마다 총 가격을 totalPrice에 추가

    // 주문 총 가격 계산 <나중에 쓰는지 확인>
    public int getTotalPrice(){
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
    // 주문 취소
    public void cancelOrder(){
        this.orderStatus = OrderStatus.CANCEL;

        for(OrderItem orderItem : orderItems){
            orderItem.cancel();
        }
        member.setMembership(member.getMembership()-this.goMembership);

        System.out.println("usedMembership 환불해주기전" + usedMembership);

        //기존 합친거에서 추가함
        if (this.usedMembership > 0) {
            this.member.setMembership(this.member.getMembership() + this.usedMembership);
            System.out.println("usedMembership 환불해주기후" + usedMembership);
            this.usedMembership = 0;
        }
    }
}
