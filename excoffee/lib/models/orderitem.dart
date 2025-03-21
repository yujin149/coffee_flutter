class OrderItem {
  final String itemNm;
  final int count;
  final int orderPrice;
  final String imgUrl;

  OrderItem({
    required this.itemNm,
    required this.count,
    required this.orderPrice,
    required this.imgUrl,
  });

  factory OrderItem.fromJson(Map<String, dynamic> json) {
    return OrderItem(
      itemNm: json['itemNm'] ?? 'Unknown Item', // null인 경우 기본값
      count: json['count'] ?? 0, // null인 경우 기본값 0
      orderPrice: json['orderPrice'] ?? 0,
      imgUrl: json['imgUrl'] ?? '',
    );
  }
}