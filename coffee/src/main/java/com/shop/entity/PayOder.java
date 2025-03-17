//package com.shop.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@Table(name = "PayOders")
//public class PayOder {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    Long id;
//    Long productId;
//    String productName;
//    int price;
//    int quantity;
//    String impUid;
//    String merchantUid;
//
//    @ManyToOne
//    @JoinColumn(name = "order_id")
//    private Order order;
//
//}