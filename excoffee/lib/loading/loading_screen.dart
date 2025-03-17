import 'package:excoffee/main_screen.dart';
import 'package:flutter/material.dart';

class LoadingScreen extends StatefulWidget {
  const LoadingScreen({super.key});

  @override
  State<LoadingScreen> createState() => _LoadingScreenState();
}

class _LoadingScreenState extends State<LoadingScreen> {
  double _opacity = 0.0;

  @override
  void initState() {
    super.initState();
    _startAnimation();
  }

  /*
  Future.delayed(const Duration(seconds: 1), () {
    setState(() {
      _opacity = 1.0; // 1초 후에 이미지를 보이게 설정
    });
  * */
  void _startAnimation() async {
    if (!mounted) return;
    
    await Future.delayed(const Duration(seconds: 1));
    if (!mounted) return;
    
    setState(() {
      _opacity = 1.0;
    });

    /*
    // 3초 후에 MainScreen으로 이동
      Future.delayed(const Duration(seconds: 2), () {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => const MainScreen()),
        );
      });
    });

    * */

    await Future.delayed(const Duration(seconds: 2));
    if (!mounted) return;

    if (context.mounted) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => const MainScreen()),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: AnimatedOpacity(
          opacity: _opacity, // 애니메이션 효과로 opacity 값이 변경됨
          duration: const Duration(seconds: 2), // 2초 동안 애니메이션
          child: Image.asset(
            'images/logo.png',
            height: 170,
            width: 200,
          ),
        ),
      ),
    );
  }
}