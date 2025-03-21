import 'package:excoffee/models/member.dart';
import 'package:excoffee/models/orderitem.dart';

class Order {
  final int orderId;
  final String orderDate;
  final String orderStatus;
  final List<OrderItem> orderItemDtoList;
  final Member member;
  final int usedMembership;
  final int finalPrice;

  Order({
    required this.orderId,
    required this.orderDate,
    required this.orderStatus,
    required this.orderItemDtoList,
    required this.member,
    required this.usedMembership,
    required this.finalPrice,
  });

  // JSON 데이터를 받아서 Order 객체로 변환하는 팩토리 메서드
  factory Order.fromJson(Map<String, dynamic> json) {
    return Order(
      orderId: json['orderId'] ?? 0, // null인 경우 기본값 0
      orderDate: json['orderDate'] ?? '', // null인 경우 빈 문자열
      orderStatus: json['orderStatus'] ?? 'UNKNOWN', // null인 경우 기본값 'UNKNOWN'
      orderItemDtoList: (json['orderItemDtoList'] as List?)?.map((item) => OrderItem.fromJson(item)).toList() ?? [], // null인 경우 빈 리스트
      member: json['member'] != null ? Member.fromJson(json['member']) : Member.empty(), // null인 경우 빈 객체
      usedMembership: json['usedMembership'] ?? 0, // null인 경우 기본값 0
      finalPrice: json['finalPrice'] ?? 0, // null인 경우 기본값 0
    );
  }
}