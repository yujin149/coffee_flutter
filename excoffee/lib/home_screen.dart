import 'package:flutter/material.dart';
import 'package:excoffee/main_screen.dart';
import 'package:excoffee/store/store.dart';
import 'package:excoffee/board/board.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 0;
  
  final List<Widget> _pages = [
    const MainScreen(),
    const Store(),
    const Board(),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _pages[_selectedIndex],
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.home),
            label: '홈',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.store),
            label: '매장안내',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.announcement),
            label: '공지사항',
          ),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: const Color(0xFFEE3424),
        onTap: _onItemTapped,
      ),
    );
  }
} 