import 'package:flutter/foundation.dart';

enum ItemMenu {
  best,
  coffee,
  desert,
  bean,
}

class Item {
  final int id;
  final String name;
  final String detail;
  final String imageUrl;
  final int price;
  final ItemMenu category; // ✅ 카테고리 추가

  Item({
    required this.id,
    required this.name,
    required this.detail,
    required this.imageUrl,
    required this.price,
    required this.category,
  });

  factory Item.fromJson(Map<String, dynamic> json) {
    try {
      return Item(
        id: json['id'],
        name: json['itemNm'],
        detail: json['itemDetail'],
        imageUrl: json['imgUrl'],
        price: json['price'],
        category: ItemMenu.values.firstWhere(
              (e) => e.name == json['category'], // 서버 응답의 'category'에 맞게 파싱
          orElse: () => ItemMenu.best,
        ),
      );
    } catch (e) {
      debugPrint('Error parsing Item from JSON: $json');
      rethrow;
    }
  }
}
