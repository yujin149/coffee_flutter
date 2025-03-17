import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:provider/provider.dart';
import '../provider/member_probider.dart';
import 'package:excoffee/member/memberform.dart';
import 'package:excoffee/main_screen.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class LoginPage extends StatefulWidget {
  @override
  _LoginPageState createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _useridController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();


  final storage = FlutterSecureStorage();

  Future<void> login(MemberProvider memberProvider) async {
    if (!_formKey.currentState!.validate()) return;

    await memberProvider.login(_useridController.text, _passwordController.text);

    // 로그인 성공 후 처리
    if (memberProvider.isLoggedIn) {
      // ✅ SnackBar 메시지 표시
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text("로그인 성공!"),
          duration: Duration(seconds: 2),
          backgroundColor: Colors.green,
        ),
      );

      // 1초 후에 메인 화면으로 이동 (메시지가 보일 시간 확보)
      Future.delayed(Duration(seconds: 1), () {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => const MainScreen()),
        );
      });

    } else {
      print("로그인 실패");
      _showLoginErrorDialog();
    }
  }

  void _showLoginErrorDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text("로그인 실패"),
        content: Text("아이디 또는 비밀번호를 확인하세요."),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text("확인"),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    MemberProvider memberProvider = Provider.of<MemberProvider>(context, listen: false);

    return Scaffold(
      appBar: AppBar(title: Text("로그인")),
      body: Padding(
        padding: EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              // 아이디 입력 필드
              Container(
                margin: EdgeInsets.only(bottom: 16.0),
                child: TextFormField(
                  controller: _useridController,
                  decoration: InputDecoration(
                    labelText: "아이디",
                    prefixIcon: Padding(
                      padding: EdgeInsets.all(10.0),
                      child: Image.asset(
                        'images/header_my.png', // 아이디 아이콘 이미지
                        width: 20,
                        height: 20,
                      ),
                    ),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: BorderSide(color: Colors.grey),
                    ),
                  ),
                  validator: (value) => value!.isEmpty ? "아이디를 입력하세요" : null,
                ),
              ),
              // 비밀번호 입력 필드
              Container(
                margin: EdgeInsets.only(bottom: 16.0),
                child: TextFormField(
                  controller: _passwordController,
                  obscureText: true,
                  decoration: InputDecoration(
                    labelText: "비밀번호",
                    prefixIcon: Padding(
                      padding: EdgeInsets.all(10.0),
                      child: Image.asset(
                        'images/login_pw.png', // 비밀번호 아이콘 이미지
                        width: 20,
                        height: 20,
                      ),
                    ),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: BorderSide(color: Colors.grey),
                    ),
                  ),
                  validator: (value) => value!.isEmpty ? "비밀번호를 입력하세요" : null,
                ),
              ),
              // 로그인 버튼
              ElevatedButton(
                onPressed: () => login(memberProvider),
                style: ElevatedButton.styleFrom(
                  minimumSize: Size(double.infinity, 50), // 전체 너비로 설정
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10),
                  ),
                  backgroundColor: Colors.blue, // 버튼 색상
                ),
                child: Text(
                  "로그인",
                  style: TextStyle(fontSize: 16),
                ),
              ),
              // 회원가입 링크
              TextButton(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => MemberForm()),
                  );
                },
                child: Text("회원가입", style: TextStyle(fontSize: 16)),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 16.0),
                child: Container(
                  width: double.infinity,
                  height: 1,
                  color: Colors.grey[300],
                ),
              ),
              // SNS 로그인 (구글, 카카오, 네이버 등)
              Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _socialLoginButton(
                    'images/go.png',
                    "구글 아이디로 로그인",
                    Colors.blue,
                  ),
                  _socialLoginButton(
                    'images/ka.png',
                    "카카오 아이디로 로그인",
                    Color(0xFFFEE500), // 카카오 색상
                  ),
                  _socialLoginButton(
                    'images/na.png',
                    "네이버 아이디로 로그인",
                    Color(0xFF03C75A), // 네이버 색상
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
  // SNS 로그인 버튼 위젯
  Widget _socialLoginButton(String imagePath, String text, Color buttonColor) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: buttonColor, // 배경 색상
          foregroundColor: Colors.white, // 텍스트 색상
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          minimumSize: Size(double.infinity, 50), // 버튼 크기
        ),
        onPressed: () {
          print('$text 클릭됨');
        },
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image.asset(
              imagePath,
              height: 20, // 이미지 크기를 20으로 조정
              width: 20, // 이미지 크기를 20으로 조정
            ),
            SizedBox(width: 10),
            Text(
              text,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}