import 'dart:async';

import 'package:flutter/material.dart';
import 'package:excoffee/member/memberloginform.dart';
import 'package:excoffee/member/memberform.dart';
import 'package:provider/provider.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:excoffee/provider/member_probider.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  _MainScreenState createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  // 이미지 리스트를 인덱스와 함께 관리
  final List<String> _imageList = [
    'images/slide/main01.jpg',
    'images/slide/main02.jpg',
    'images/slide/main03.jpg',
    'images/slide/main04.jpg'
  ];

  int _currentIndex = 0; // 현재 이미지 인덱스
  Timer? _timer;  // Timer 변수 추가

  @override
  void initState() {
    super.initState();
    // 2초마다 이미지 인덱스를 변경하는 타이머
    /*
     Timer.periodic(Duration(seconds: 2), (Timer timer) {
      setState(() {
        _currentIndex = (_currentIndex + 1) % _imageList.length;
      });
     */

    //위의 코드는 화면에서 사라져도 이미지 슬라이더가 계속 실행되서 위젯이 화면에 있는지 먼저 확인하기 위해 mounted를 추가
    //
    _timer = Timer.periodic(Duration(seconds: 2), (Timer timer) {
      if (mounted) {  // mounted 체크 추가
        setState(() {
          _currentIndex = (_currentIndex + 1) % _imageList.length;
        });
      }
    });
  }

  @override
  void dispose() {
    _timer?.cancel();  // Timer 취소
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Main Page'),
        actions: [
          Consumer<MemberProvider>(
            builder: (context, memberProvider, child) {
              bool isLoggedIn = memberProvider.isLoggedIn;

              return Row(
                children: [
                  if (!isLoggedIn) // 로그인되지 않은 경우에만 보이게
                    IconButton(
                      icon: const Icon(Icons.app_registration),
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => MemberForm()),
                        );
                      },
                    ),
                  IconButton(
                    icon: Icon(isLoggedIn ? Icons.logout : Icons.login),
                    onPressed: () async {
                      if (isLoggedIn) {
                        await memberProvider.logOut();
                        Navigator.pushReplacement(
                          context,
                          MaterialPageRoute(builder: (context) => const MainScreen()),
                        );
                      } else {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => LoginPage()),
                        );
                      }
                    },
                  ),
                ],
              );
            },
          ),
        ],

      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            // 슬라이더 (자동으로 바뀌는 이미지)
            Container(
              height: 200,
              child: Image.asset(
                _imageList[_currentIndex], // 현재 인덱스에 해당하는 이미지
                fit: BoxFit.cover,
              ),
            ),
            // 전체 상품
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Text(
                '전체 상품',
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              ),
            ),
            ListView.builder(
              shrinkWrap: true,
              physics: NeverScrollableScrollPhysics(),
              itemCount: 8,
              itemBuilder: (context, index) {
                return Card(
                  child: ListTile(
                    leading: Image.asset(
                      'images/product${index + 1}.jpg',
                      width: 100,
                      height: 100,
                      fit: BoxFit.cover,
                    ),
                    title: Text('상품 ${index + 1}'),
                    subtitle: Text('${(index + 1) * 1000}원'),
                    onTap: () {
                      // 상품 상세 페이지로 이동
                    },
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}