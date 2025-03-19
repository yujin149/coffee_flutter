import 'dart:convert';

import 'package:excoffee/models/member.dart';
import 'package:flutter/material.dart';

import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class MemberProvider extends ChangeNotifier {


  Member _memberInfo = Member();
  bool _isLoggedIn = false;

  // ✅ getter
  Member get memberInfo => _memberInfo;
  bool get isLoggedIn => _isLoggedIn;

  // 🔒 안전한 저장소
  final storage = const FlutterSecureStorage();
  // 읽기 : storage.read(key: key)
  // 쓰기 : storage.write(key: key, value: value)
  // 삭제 : storage.delete(key: key)
  void setLoggedIn(bool isLoggedIn) {
    _isLoggedIn = isLoggedIn;
    notifyListeners(); // 상태 변경을 UI에 반영하도록 알림
  }

/// 🔐 로그인 요청
/// 1. 요청 및 응답
/// ➡ userid, password
/// ⬅ jwt token
///
/// 2. jwt 토큰을 SecureStorage 에 저장
  Future<void> login(String userid, String password) async {

    //const url = 'http://10.0.2.2:8080/members/api/login'; // 로그인 경로
    const url = 'http://192.168.0.37:8080/members/api/login'; // 로그인 경로
    final requestUrl = Uri.parse(url);
    try {
      // 로그인 요청
      final response = await http.post(
        requestUrl,
        headers: {"Content-Type": "application/json"},
        body: json.encode({
          "userid": userid,
          "password": password,
        }),
      );

      if (response.statusCode == 200) {
        print('로그인 성공...');
        print('aaaa');

        // 응답 본문에서 토큰 추출
        final responseBody = json.decode(response.body);
        final jwtToken = responseBody['token'];

        if (jwtToken != null) {
          // JWT 토큰을 안전한 저장소에 저장
          await storage.write(key: 'jwtToken', value: jwtToken);
          _isLoggedIn = true;
          notifyListeners();

          print('JWT Token: $jwtToken');

          // 로그인 후 사용자 정보 요청
          await getMemberInfo();  // 바로 사용자 정보 가져오기

        } else {
          print('토큰이 응답 본문에 없습니다.');
        }
      } else if (response.statusCode == 403) {
        print('아이디 또는 비밀번호가 일치하지 않습니다...');
      } else {
        print('네트워크 오류 또는 알 수 없는 오류로 로그인에 실패하였습니다...');
      }
    } catch (error) {
      print('로그인 실패 $error');
    }
    notifyListeners();
  }

/// 👩‍💼👨‍💼 사용자 정보 가져오기
/// 1. 💍 jwt ➡ 서버
/// 2. 클라이언트 ⬅ 👩‍💼👨‍💼
/// 3. 👩‍💼👨‍💼(memberInfo) ➡ _memberInfo [provider] 저장
   Future<void> getMemberInfo() async{
     final jwtToken = await storage.read(key: 'jwtToken');

     if (jwtToken == null) {
       print("토큰이 존재하지 않습니다.");
       return;
     }

     //final url =  'http://10.0.2.2:8080/members/api/info'; // 사용자 정보 요청 경로
     final url =  'http://192.168.0.37:8080/members/api/info'; // 사용자 정보 요청 경로
     try {
       // 저장된 jwt 가져오기
       String? token = await storage.read(key: 'jwtToken');
       print('사용자 정보 요청 전: jwt - $token');
//jwt - eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJleHAiOjE3MDY5NTEzMTgsInVubyI6IjI0IiwidWlkIjoiY2h1eWoyIiwicm9sIjpbIlJPTEVfU0VMTCJdfQ.RavcKqDokDQrWU2oK4yRGV1paoGWsQrQ7gUb4WhgNgFaOxtOjp35YMY58lZWuZV4zbJzEIfN1LQtstUG9ztntg
       final response = await http.get(
         Uri.parse(url),
         headers: {
           'Authorization': 'Bearer $token',
           'Content-Type': 'application/json',
         },
       );

       if(response.statusCode == 200) {
         // 성공적으로 데이터를 받아왔을 때의 처리
         var utf8Decoded = utf8.decode(response.bodyBytes);
         var result = json.decode(utf8Decoded);
         final memberInfo = result;
         print('Member Info: $memberInfo');
         // provider 에 사용자 정보 저장
         // memberInfo ➡ _memberInfo 로 저장
         // provider  등록
         _memberInfo = Member.fromJson(memberInfo);
         setLoggedIn(true);
         print(_memberInfo);
       } else {
         // HTTP 요청이 실패했을 때의 처리
         print('HTTP 요청 실패: ${response.statusCode}');

         print('사용자 정보 요청 성공');
       }
     }
     catch (error){
       print('사용자 정보 요청 실패 $error');
     }
     notifyListeners();
   }
  //로그아웃
   Future<void> logOut() async {
    try{
      // ⬅👨‍💼 로그아웃 처리
      // jwt 토큰 삭제
      await storage.delete(key: 'jwtToken');
      // 사용자 정보 초기화
      _memberInfo = Member();
      // 로그인 상태 초기화
      _isLoggedIn = false;

      print('로그아웃 성공');
    } catch (error) {
      print('로그아웃 실패');
    }
    print('notifyListeners 요청됨');
    notifyListeners();
   }


  Future<void> checkLoginStatus() async {
    String? token = await storage.read(key: 'jwtToken');
    if (token != null) {
      _isLoggedIn = true;
      await getMemberInfo();
    }
    notifyListeners();
  }


  // Future<void> socialLogin(String provider, String authCode) async {
  //   final url = 'http://10.0.2.2:8080/api/oauth2/google';
  //   final requestUrl = Uri.parse(url);
  //
  //   try {
  //     final response = await http.post(
  //       requestUrl,
  //       headers: {"Content-Type": "application/json"},
  //       body: json.encode({"authCode": authCode}),
  //     );
  //
  //     if (response.statusCode == 200) {
  //       final responseBody = json.decode(response.body);
  //       final jwtToken = responseBody['token'];
  //
  //       if (jwtToken != null) {
  //         // ✅ JWT 토큰 저장
  //         await storage.write(key: 'jwtToken', value: jwtToken);
  //         _isLoggedIn = true;
  //         notifyListeners();
  //
  //         print('소셜 로그인 성공! JWT: $jwtToken');
  //
  //         // ✅ 사용자 정보 요청
  //         await getMemberInfo();
  //       } else {
  //         print('소셜 로그인 실패: 응답에 토큰 없음');
  //       }
  //     } else {
  //       print('소셜 로그인 실패: ${response.statusCode}');
  //     }
  //   } catch (error) {
  //     print('소셜 로그인 요청 중 오류 발생: $error');
  //   }
  // }
}