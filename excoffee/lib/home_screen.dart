import 'package:flutter/material.dart';
import 'package:excoffee/main_screen.dart';
import 'package:excoffee/store/store.dart';
import 'package:excoffee/board/board.dart';
import 'package:excoffee/gallery/gallery.dart';

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
    const Gallery(),
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
        type: BottomNavigationBarType.fixed,
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
          BottomNavigationBarItem(
            icon: Icon(Icons.event),
            label: '이벤트',
          ),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: const Color(0xFFEE3424),
        unselectedItemColor: const Color(0xFF888888),
        selectedLabelStyle: const TextStyle(fontSize: 12),
        unselectedLabelStyle: const TextStyle(fontSize: 12),
        showUnselectedLabels: true,
        onTap: _onItemTapped,
      ),
    );
  }
} 