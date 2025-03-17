import 'package:excoffee/loading/loading_screen.dart';
import 'package:excoffee/provider/member_probider.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:excoffee/member/memberform.dart';
import 'package:excoffee/member/memberloginform.dart';
import 'package:excoffee/home_screen.dart';
import 'package:excoffee/board/board.dart';
import 'package:excoffee/board/detail.dart';
import 'package:flutter/foundation.dart';

void main() async {
  // Flutter 바인딩 초기화
  WidgetsFlutterBinding.ensureInitialized();
  
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => MemberProvider()),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    // 앱 시작 시 로그인 상태 확인
    Provider.of<MemberProvider>(context, listen: false).getMemberInfo();

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Coffee App',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.brown),
        useMaterial3: true,
      ),
      routes: {
        '/board': (context) => const Board(),
        '/board/detail': (context) => const BoardDetail(),
      },
      home: FutureBuilder(
        future: Future.delayed(const Duration(seconds: 2)), // 로딩 화면을 2초 동안 보여주기
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const LoadingScreen();
          } else {
            return const HomeScreen();
          }
        },
      ),
    );
  }
}