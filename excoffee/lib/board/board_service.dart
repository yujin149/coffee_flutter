import 'dart:convert';
import 'package:http/http.dart' as http;
import 'board_model.dart';

class BoardService {
  // 실제 모바일 기기에서 테스트할 때는 컴퓨터의 실제 IP 주소를 사용
  static const String baseUrl = 'http://192.168.0.37:8080/api';

  // 안드로이드 에뮬레이터용 서버 URL
  static const String baseUrlEmulator = 'http://10.0.2.2:8080/api';

  // 게시글 목록 조회
  Future<Map<String, dynamic>> getBoards({
    int page = 0,
    String? searchQuery,
    String? searchBy,
  }) async {
    try {
      // URL 파라미터 구성
      final queryParams = {
        'page': page.toString(),
        if (searchQuery != null && searchQuery.isNotEmpty) 'searchQuery': searchQuery,
        if (searchBy != null && searchBy.isNotEmpty) 'searchBy': searchBy,
      };

      final uri = Uri.parse('$baseUrl/board').replace(queryParameters: queryParams);
      print('Requesting URL: $uri');

      final response = await http.get(uri);
      print('Response status code: ${response.statusCode}');
      print('Response headers: ${response.headers}');
      
      // UTF-8로 디코딩
      final decodedBody = utf8.decode(response.bodyBytes);
      print('Response body: $decodedBody');

      if (response.statusCode == 200) {
        final Map<String, dynamic> data = json.decode(decodedBody);
        List<BoardItem> allBoards = [];
        
        // 공지사항 처리
        if (data['noticeBoards'] != null) {
          final List<dynamic> noticeBoards = data['noticeBoards'];
          print('Notice boards count: ${noticeBoards.length}');
          print('Notice boards data: $noticeBoards');
          allBoards.addAll(noticeBoards.map((item) => BoardItem(
            number: item['id'],
            title: item['title'],
            regDate: item['regTime'].toString().split('T')[0],
            boardStatus: BoardStatus.NOTICE,
          )));
        }
        
        // 일반 게시글 처리
        int totalPages = 1;
        if (data['boards'] != null) {
          if (data['boards']['content'] != null) {
            final List<dynamic> generalBoards = data['boards']['content'];
            print('General boards count: ${generalBoards.length}');
            print('General boards data: $generalBoards');
            allBoards.addAll(generalBoards.map((item) => BoardItem(
              number: item['id'],
              title: item['title'],
              regDate: item['regTime'].toString().split('T')[0],
              boardStatus: BoardStatus.GENERAL,
            )));
          }
          // 서버에서 받아온 총 페이지 수 사용
          totalPages = data['boards']['totalPages'] ?? 1;
        }
        
        print('Total boards count: ${allBoards.length}');
        print('Total pages: $totalPages');
        
        return {
          'boards': allBoards,
          'totalPages': totalPages,
        };
      } else {
        print('Server error: ${response.statusCode}');
        print('Error response: $decodedBody');
        throw Exception('Failed to load boards: ${response.statusCode} - $decodedBody');
      }
    } catch (e) {
      print('Error in getBoards: $e');
      if (e is FormatException) {
        throw Exception('서버 응답 형식이 올바르지 않습니다: $e');
      } else if (e is http.ClientException) {
        throw Exception('서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.');
      } else {
        throw Exception('게시글을 불러오는 중 오류가 발생했습니다: $e');
      }
    }
  }

  // 게시글 상세 조회
  Future<Map<String, dynamic>> getBoardDetail(int boardNumber) async {
    final url = Uri.parse('$baseUrl/board/$boardNumber');
    
    try {
      print('Requesting board detail: $url'); // URL 로그
      final response = await http.get(url);
      print('Response status: ${response.statusCode}'); // 상태 코드 로그
      
      if (response.statusCode == 200) {
        final data = utf8.decode(response.bodyBytes);
        print('Response data: $data'); // 응답 데이터 로그
        final Map<String, dynamic> jsonData = json.decode(data);
        
        if (jsonData['board'] != null) {
          final board = jsonData['board'];
          print('Board data: $board'); // 게시글 데이터 로그
          
          return {
            'title': board['title'] ?? '',
            'content': board['content'] ?? '',
            'writer': board['writer'] ?? '',
            'regDate': board['regTime']?.toString()?.split('T')?[0] ?? '',
            'boardStatus': board['boardStatus'] ?? board['board_status'] ?? 'GENERAL',
          };
        }
        throw Exception('게시글을 찾을 수 없습니다.');
      }
      throw Exception('게시글 조회에 실패했습니다. (상태 코드: ${response.statusCode})');
    } catch (e) {
      print('Error in getBoardDetail: $e'); // 에러 로그
      throw Exception('게시글 조회 중 오류가 발생했습니다: $e');
    }
  }
} 