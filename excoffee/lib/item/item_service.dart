import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/item.dart';

class ItemService {
  //static const String baseUrl = 'http://10.0.2.2:8080/api/main/items';
  static const String baseUrl = 'http://192.168.0.37:8080/api/main/items';

  Future<Map<String, dynamic>> fetchItems({int page = 0}) async {
    try {
      final uri = Uri.parse('$baseUrl?page=$page');
      final response = await http.get(uri);

      if (response.statusCode == 200) {
        // 응답을 UTF-8로 디코딩
        final decodedBody = utf8.decode(response.bodyBytes);
        final Map<String, dynamic> data = json.decode(decodedBody);



        // 'items' 리스트를 받아서 Item 객체로 변환
        List<Item> items = [];
        if (data['items'] != null) {
          final List<dynamic> itemList = data['items'];
          items = itemList.map((item) => Item.fromJson(item)).toList();
        }
        // 'totalPages' 추출
        int totalPages = data['totalPages'] ?? 1;
        int currentPage = data['currentPage'] ?? 0;

        // 'items'와 'totalPages'를 Map으로 반환
        return {
          'items': items,
          'totalPages': totalPages,
          'currentPage': currentPage,
        };
      } else {
        throw Exception('Failed to load items');
      }
    } catch (e) {
      throw Exception('Failed to load items: $e');
    }
  }
  Future<Item> fetchItemDetail(int itemId) async {
    final uri = Uri.parse('$baseUrl/$itemId'); // 여기에 찍어보자
    print('📦 요청 URI: $uri');
    final response = await http.get(uri);

    if (response.statusCode == 200) {
      final decodedBody = utf8.decode(response.bodyBytes);
      final jsonData = json.decode(decodedBody);
      return Item.fromJson(jsonData);
    } else {
      throw Exception('Failed to load item detail');
    }
  }


}