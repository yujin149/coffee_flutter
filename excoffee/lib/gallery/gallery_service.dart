import 'dart:convert';
import 'package:http/http.dart' as http;
import 'gallery_model.dart';

class GalleryService {
  //static const String baseUrl = 'http://192.168.0.37:8080';
  static const String baseUrl = 'http://10.0.2.2:8080';

  Future<Map<String, dynamic>> getGalleries({
    required int page,
    String searchQuery = '',
    String searchType = 'all',
  }) async {
    try {
      final response = await http.get(
        Uri.parse(
          '$baseUrl/api/gallery?page=$page&keyword=$searchQuery&searchType=$searchType',
        ),
      );

      if (response.statusCode == 200) {
        // UTF-8로 디코딩
        final decodedBody = utf8.decode(response.bodyBytes);
        final Map<String, dynamic> data = json.decode(decodedBody);
        final List<dynamic> galleriesData = data['galleries'];
        final bool hasMore = data['hasMore'];

        final List<GalleryItem> galleries = galleriesData.map((json) {
          // 대표 이미지 URL 처리
          if (json['repImgUrl'] != null && json['repImgUrl'].toString().startsWith('/')) {
            json['repImgUrl'] = '$baseUrl${json['repImgUrl']}';
          }
          
          // 갤러리 이미지 목록 처리
          if (json['galleryImgList'] != null) {
            final List<dynamic> imgList = json['galleryImgList'];
            json['galleryImgList'] = imgList.map((imgUrl) {
              if (imgUrl.toString().startsWith('/')) {
                return '$baseUrl$imgUrl';
              }
              return imgUrl;
            }).toList();
          } else {
            json['galleryImgList'] = <String>[];
          }
          
          return GalleryItem.fromJson(json);
        }).toList();

        return {
          'galleries': galleries,
          'hasMore': hasMore,
        };
      } else {
        throw Exception('게시글을 불러오는 중 오류가 발생했습니다');
      }
    } catch (e) {
      throw Exception('Error: $e');
    }
  }
}
