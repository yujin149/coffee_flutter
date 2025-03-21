import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:provider/provider.dart';
import '../provider/member_probider.dart';
import 'package:excoffee/member/memberform.dart';
import 'package:excoffee/main_screen.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:excoffee/home_screen.dart';


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
          MaterialPageRoute(builder: (context) => const HomeScreen()),
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

  //구글

  final GoogleSignIn _googleSignIn = GoogleSignIn(
      scopes: ['email', 'profile'],
      serverClientId: "349112837672-ooouh4qkmottcue3v40vji2qr470bpmj.apps.googleusercontent.com"
    // signInOption: SignInOption.standard,
  );
  Future<void> signInWithGoogle(BuildContext context) async {
    try {
      // 기존의 로그인된 계정에서 토큰을 받기 위해 사용
      GoogleSignInAccount? googleUser = await _googleSignIn.currentUser;
      print("googleUser : $googleUser");

      if (googleUser == null) {
        // 만약 이미 로그인된 계정이 없다면 새로 로그인 진행
        googleUser = await _googleSignIn.signIn();
      }
      if (googleUser == null) {
        print("사용자가 로그인 취소");
        return;
      }

      final GoogleSignInAuthentication googleAuth = await googleUser.authentication;
      final String? accessToken = googleAuth.accessToken;
      final String? idToken = googleAuth.idToken;

      print("googleAuth : $googleAuth");
      print("accessToken : $accessToken");
      print("idToken : $idToken");

      if (accessToken != null && idToken != null) {
        print("sendGoogleTokenToBackend");
        // 백엔드로 토큰 전달
        await sendGoogleTokenToBackend(context, accessToken, idToken);
      } else {
        print("Google 로그인 실패: 토큰이 유효하지 않음");
      }
    } catch (error) {
      print("Google 로그인 오류: $error");
    }
  }

  Future<void> sendGoogleTokenToBackend(BuildContext context, String accessToken, String idToken) async {
    final MemberProvider memberProvider = Provider.of<MemberProvider>(context, listen: false);

    final response = await http.post(
      Uri.parse('http://10.0.2.2:8080/api/oauth2/google'),
      //Uri.parse('http://192.168.0.37:8080/api/oauth2/google'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'accessToken': accessToken, 'idToken': idToken}),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      final String jwtToken = data['token'];

      if (jwtToken != null) {

        await storage.write(key: 'jwtToken', value: jwtToken);
        // 정상적으로 JWT 토큰을 받아왔을 때
        String? savedToken = await storage.read(key: 'jwtToken');
        print("저장된 JWT 토큰 확인: $savedToken");

        if (savedToken != null) {
          print("JWT 저장 완료: $savedToken");
          // 사용자 정보 가져오기
          await memberProvider.getMemberInfo();

          if (memberProvider.isLoggedIn) {
            // ✅ SnackBar 메시지 표시
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text("구글 로그인 성공!"),
                duration: Duration(seconds: 2),
                backgroundColor: Colors.green,
              ),
            );

            // 1초 후에 메인 화면으로 이동 (메시지가 보일 시간 확보)
            Future.delayed(Duration(seconds: 1), () {
              Navigator.pushReplacement(
                context,
                MaterialPageRoute(builder: (context) => HomeScreen()),
              );
            });

          } else {
            print("로그인 실패");
            _showLoginErrorDialog();
          }
        } else {
          print("저장된 토큰이 없습니다.");
        }
      } else {
        print("JWT 토큰이 응답에 포함되어 있지 않습니다.");
      }
    } else {
      print("소셜 로그인 실패: ${response.body}");
    }
  }



  @override
  Widget build(BuildContext context) {
    MemberProvider memberProvider = Provider.of<MemberProvider>(context, listen: false);

    return Scaffold(
      appBar: AppBar(title: Text("로그인")),
      body: SingleChildScrollView(
        child: Padding(
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
                    foregroundColor: Colors.white,
                    backgroundColor: Color(0xFFEE3424), // 버튼 색상
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
                    ElevatedButton(
                      onPressed: () => signInWithGoogle(context),
                      style: ElevatedButton.styleFrom(
                        foregroundColor: Colors.white,
                        backgroundColor: Color(0xFFEE3424),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        minimumSize: Size(double.infinity, 50),
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Image.asset('images/go.png', height: 20),
                          SizedBox(width: 10),
                          Text("구글 아이디로 로그인", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                        ],
                      ),
                    ),
                    _socialLoginButton(
                      'images/ka.png',
                      "카카오 아이디로 로그인",
                      Color(0xFFFEE500), // 카카오 색상
                      "kakao"
                    ),
                    _socialLoginButton(
                      'images/na.png',
                      "네이버 아이디로 로그인",
                      Color(0xFF03C75A), // 네이버 색상
                      "naver"
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  // SNS 로그인 버튼 위젯
  Widget _socialLoginButton(String imagePath, String text, Color buttonColor, String provider) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: buttonColor,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          minimumSize: Size(double.infinity, 50),
        ),
        onPressed: () {
          // 네이버와 카카오는 웹에서만 가능하다는 메시지 표시
          if (provider == 'kakao' || provider == 'naver') {
            _showWebLoginMessage();
          }
        },
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image.asset(
              imagePath,
              height: 20,
              width: 20,
            ),
            SizedBox(width: 10),
            Text(
              text,
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ),
    );
  }

  void _showWebLoginMessage() {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
            "네이버와 카카오는 웹에서만 로그인 가능합니다."),
        duration: Duration(seconds: 2),
        backgroundColor: Colors.orange,
      ),
    );
  }
}