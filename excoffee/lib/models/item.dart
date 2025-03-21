import 'package:flutter/foundation.dart';

class Item {
  final int id;
  final String name;
  final String detail;
  final String imageUrl;
  final int price;

  Item({
    required this.id,
    required this.name,
    required this.detail,
    required this.imageUrl,
    required this.price,
  });

  factory Item.fromJson(Map<String, dynamic> json) {
    try {
      return Item(
        id: json['id'],
        name: json['itemNm'],
        detail: json['itemDetail'],
        imageUrl: json['imgUrl'],
        price: json['price'],
      );
    } catch (e) {
      debugPrint('Error parsing Item from JSON: $json');
      rethrow;  // 오류 발생 시 재처리
    }
  }
}