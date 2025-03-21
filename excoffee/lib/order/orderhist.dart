import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:excoffee/member/memberloginform.dart';
import 'package:excoffee/models/order.dart';

class OrderHistPage extends StatefulWidget {
  @override
  _OrderHistPageState createState() => _OrderHistPageState();
}

class _OrderHistPageState extends State<OrderHistPage> {
  List<Order> orders = [];
  int totalPages = 0;
  int currentPage = 0;
  final storage = FlutterSecureStorage();

  @override
  void initState() {
    super.initState();
    fetchOrders(currentPage);
  }

  Future<String?> getUserId() async {
    return await storage.read(key: 'userId');  // 'userId'가 로그인 후 저장된 값이라고 가정
  }

  Future<void> fetchOrders(int page) async {
    String? userId = await getUserId();  // JWT 대신 사용자 ID로 인증

    print("userId: $userId");

    if (userId == null) {
      print("사용자 ID가 존재하지 않습니다.");
      Navigator.push(
        context,
        MaterialPageRoute(builder: (context) => LoginPage()),
      );
      return;
    }

    final response = await http.get(
      Uri.parse('http://10.0.2.2:8080/orders/api/$userId'),
      headers: {
        "Content-Type": "application/json",
      },
    );

    if (response.statusCode == 200) {
      try {
        // UTF-8 디코딩 적용
        final decodedBody = utf8.decode(response.bodyBytes);
        final Map<String, dynamic> data = json.decode(decodedBody); // JSON을 Map으로 변환

        print('decodedBody : $decodedBody');
        print('data : $data');

        List<dynamic> ordersJson = data["orders"] ?? []; // "orders" 키에서 리스트 추출
        totalPages = data["totalPages"] ?? 0;

        setState(() {
          orders = ordersJson.map((order) {
            // 각 주문에 대해 null 값을 처리하고 Order 객체로 변환
            return Order.fromJson(order);
          }).toList();
        });

      } catch (e) {
        print('응답을 JSON으로 변환 실패: $e');
        print('응답 본문: ${response.body}');
      }
    } else {
      print("Failed to load orders: ${response.statusCode}");
      if (response.statusCode == 401 || response.statusCode == 403) {
        print("인증이 필요합니다.");
        Navigator.pushNamed(context, '/login');
      }
    }
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
          title: Text(
              "구매 내역",
            style: TextStyle(
              color: Colors.white,
            ),
          ),
        backgroundColor: Color(0xFFEE3424),
      ),
      body: Column(
        children: [
          Expanded(
            child: orders.isEmpty
                ? Center(child: CircularProgressIndicator())
                : ListView.builder(
              itemCount: orders.length,
              itemBuilder: (context, index) {
                final order = orders[index];
                return Container(
                  margin: EdgeInsets.all(8.0),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(10),
                    boxShadow: [
                      BoxShadow(
                        blurRadius: 5,
                        color: Colors.grey.withOpacity(0.3),
                      ),
                    ],
                  ),
                  child: Padding(
                    padding: EdgeInsets.all(8.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text("주문번호: ${order.orderId}", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                        Text("주문일자: ${order.orderDate}"),
                        Column(
                          children: order.orderItemDtoList.map((item) {
                            return ListTile(
                              leading: Image.network('http://10.0.2.2:8080${item.imgUrl}'),
                              title: Text(item.itemNm),
                              subtitle: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text("가격: ${item.orderPrice}원"),
                                  Text("수량: ${item.count}개"),
                                ],
                              ),
                            );
                          }).toList(),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  icon: Icon(Icons.chevron_left),
                  onPressed: currentPage > 0
                      ? () {
                    setState(() {
                      currentPage--;
                      fetchOrders(currentPage);
                    });
                  }
                      : null,
                ),
                SizedBox(width: 4),
                ...List.generate(5, (index) {
                  int pageNumber = currentPage + index;
                  if (pageNumber >= totalPages) {
                    return SizedBox.shrink();
                  }

                  final isCurrentPage = currentPage == pageNumber;
                  return Container(
                    width: 32,
                    height: 32,
                    margin: const EdgeInsets.symmetric(horizontal: 2),
                    child: TextButton(
                      style: TextButton.styleFrom(
                        backgroundColor: isCurrentPage ? Color(0xFFEE3424) : null,
                        foregroundColor: isCurrentPage ? Colors.white : Colors.black,
                        padding: EdgeInsets.zero,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                      ),
                      onPressed: () {
                        setState(() {
                          currentPage = pageNumber;
                        });
                        fetchOrders(currentPage);
                      },
                      child: Text(
                        (pageNumber + 1).toString(),
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: isCurrentPage ? FontWeight.bold : FontWeight.normal,
                        ),
                      ),
                    ),
                  );
                }),
                SizedBox(width: 4),
                IconButton(
                  icon: Icon(Icons.chevron_right),
                  onPressed: currentPage + 1 < totalPages
                      ? () {
                    setState(() {
                      currentPage++;
                      fetchOrders(currentPage);
                    });
                  }
                      : null,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}