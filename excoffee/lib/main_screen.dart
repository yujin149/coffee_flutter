import 'dart:async';

import 'package:flutter/material.dart';
import 'package:excoffee/member/memberloginform.dart';
import 'package:excoffee/member/memberform.dart';
import 'package:provider/provider.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:excoffee/provider/member_probider.dart';
import 'package:excoffee/home_screen.dart';
import 'package:excoffee/order/orderhist.dart';
import 'package:excoffee/models/item.dart';
import 'package:excoffee/item/item_service.dart';
import 'package:excoffee/item/item_detail.dart'; // ✅ 상세 페이지




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
  Future<List<Item>>? _itemsFuture;
  bool _isLoading = false;
  List<Item> _items = [];


  int _currentIndex = 0; // 현재 이미지 인덱스
  int _currentPage = 0;   // 현재 페이지
  int _totalPages = 1;    // 전체 페이지 수
  Timer? _timer;  // Timer 변수 추가

  ItemMenu? _selectedCategory;

  @override
  void initState() {
    super.initState();
    _fetchItems();
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

  void _fetchItems() async {
    try {
      final response = await ItemService().fetchItems(page: _currentPage);
      // final List<Item> items = await ItemService().fetchItems();
      if (mounted) {
        setState(() {
          // _itemsFuture = Future.value(items); // Future<List<Item>>로 설정
          _itemsFuture = Future.value(response['items']);
          _totalPages = response['totalPages'];
        });
      }
    } catch (error) {
      if (mounted) {
        setState(() {
          _itemsFuture = Future.error("상품 목록을 불러오는 데 실패했습니다. 오류: $error");
        });
      }
    }
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
                      icon: const Icon(Icons.person_add),
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => MemberForm()),
                        );
                      },
                    ),
                  if (isLoggedIn)
                    PopupMenuButton<String>(
                      icon: Icon(Icons.account_circle), // 마이페이지 아이콘
                      onSelected: (value) {
                        if (value == 'edit') {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                              content: Text("회원정보 수정은 웹에서만 가능합니다."),
                              duration: Duration(seconds: 2),
                              backgroundColor: Colors.orange,
                            ),
                          );
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => HomeScreen()),
                          );
                        } else if (value == 'history') {
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => OrderHistPage()),
                          );
                        }
                      },
                      itemBuilder: (context) => [
                        PopupMenuItem(
                          value: 'edit',
                          child: ListTile(
                            leading: Icon(Icons.edit, color: Colors.blue),
                            title: Text("회원정보 수정"),
                          ),
                        ),
                        PopupMenuItem(
                          value: 'history',
                          child: ListTile(
                            leading: Icon(Icons.shopping_bag, color: Colors.green),
                            title: Text("구매 내역"),
                          ),
                        ),
                      ],
                    ),
                  IconButton(
                    icon: Icon(isLoggedIn ? Icons.logout : Icons.login),
                    onPressed: () async {
                      if (isLoggedIn) {
                        await memberProvider.logOut();
                        Navigator.pushReplacement(
                          context,
                          MaterialPageRoute(builder: (context) => const HomeScreen()),
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
            FutureBuilder<List<Item>>(
              future: _itemsFuture,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return Center(child: CircularProgressIndicator());
                } else if (snapshot.hasError) {
                  return Center(child: Text(snapshot.error.toString()));
                } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                  return Center(child: Text('등록된 상품이 없습니다.'));
                } else {
                  List<Item> items = snapshot.data!;

                  // ✅ 선택된 카테고리가 있으면 필터링
                  if (_selectedCategory != null) {
                    items = items.where((item) => item.category == _selectedCategory).toList();
                  }

                  return Column(
                    children: [
                      GridView.builder(
                        shrinkWrap: true,
                        physics: NeverScrollableScrollPhysics(),
                        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 3,
                          crossAxisSpacing: 10.0,
                          mainAxisSpacing: 10.0,
                          childAspectRatio: 0.75,
                        ),
                        itemCount: items.length,
                        itemBuilder: (context, index) {
                          final item = items[index];
                          return InkWell(
                            onTap: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => ItemDetailPage(item: item),
                                ),
                              );
                            },

                            child: Container(
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(10),
                                boxShadow: [BoxShadow(blurRadius: 5, color: Colors.grey)],
                                color: Colors.white,
                              ),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.center,
                                children: [
                                  ClipRect(
                                    child: Image.network(

                                      item.imageUrl.startsWith('http')
                                          ? item.imageUrl
                                          : 'http://10.0.2.2:8080${item.imageUrl}',
                                      width: double.infinity, // 부모 크기 맞춤
                                      height: 120,
                                      fit: BoxFit.cover,
                                      errorBuilder: (context, error, stackTrace) => Container(
                                        width: double.infinity,
                                        height: 120,
                                        color: Colors.grey[300],
                                        child: Icon(Icons.image_not_supported, size: 50, color: Colors.grey),
                                      ),
                                    ),
                                  ),
                                  SizedBox(height: 8),
                                  Expanded(
                                    child: Padding(
                                      padding: EdgeInsets.symmetric(horizontal: 5),
                                      child: Text(
                                        item.name,
                                        textAlign: TextAlign.center,
                                        style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold),
                                        maxLines: 2, // 두 줄까지 표시
                                        overflow: TextOverflow.ellipsis, // 길면 "..." 처리
                                      ),
                                    ),
                                  ),
                                  SizedBox(height: 4),
                                  Text(
                                    '${item.price}원',
                                    style: TextStyle(fontSize: 12, color: Colors.red),
                                  ),
                                ],
                              ),
                            ),
                          );
                        },
                      ),
                      if (_totalPages > 1)  // 페이지가 1페이지 이상일 때만 버튼을 보이도록 설정
                        Padding(
                          padding: const EdgeInsets.symmetric(
                            vertical: 8.0,
                            horizontal: 4.0,
                          ),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              // 이전 페이지 버튼
                              IconButton(
                                icon: const Icon(Icons.chevron_left),
                                padding: const EdgeInsets.all(4),
                                constraints: const BoxConstraints(),
                                onPressed: _currentPage > 0
                                    ? () {
                                  setState(() {
                                    // 페이지가 1 이상일 때만 이동하도록 설정
                                    if (_currentPage % 5 == 0) {
                                      _currentPage = (_currentPage - 1).clamp(0, _totalPages - 1);
                                    } else {
                                      _currentPage--; // 1단위로 이동
                                    }
                                  });
                                  _fetchItems();
                                }
                                    : null,
                              ),
                              const SizedBox(width: 4),

                              // 페이지 번호 버튼들 (현재 페이지 기준 5개씩 보이도록)
                              ...List.generate(5, (index) {
                                int pageNumber = _currentPage + index;
                                if (pageNumber >= _totalPages) {
                                  return SizedBox.shrink();  // 마지막 페이지가 넘어가면 빈 공간 표시
                                }

                                final isCurrentPage = _currentPage == pageNumber;
                                return Container(
                                  width: 32,
                                  height: 32,
                                  margin: const EdgeInsets.symmetric(
                                    horizontal: 2,
                                  ),
                                  child: TextButton(
                                    style: TextButton.styleFrom(
                                      backgroundColor: isCurrentPage
                                          ? const Color(0xFFEE3424)
                                          : null,
                                      foregroundColor: isCurrentPage
                                          ? Colors.white
                                          : Colors.black,
                                      padding: EdgeInsets.zero,
                                      shape: RoundedRectangleBorder(
                                        borderRadius: BorderRadius.circular(16),
                                      ),
                                    ),
                                    onPressed: () {
                                      setState(() {
                                        _currentPage = pageNumber;
                                      });
                                      _fetchItems();
                                    },
                                    child: Text(
                                      (pageNumber + 1).toString(),  // 페이지 번호 표시
                                      style: TextStyle(
                                        fontSize: 14,
                                        fontWeight: isCurrentPage
                                            ? FontWeight.bold
                                            : FontWeight.normal,
                                      ),
                                    ),
                                  ),
                                );
                              }),

                              const SizedBox(width: 4),
                              // 다음 페이지 버튼
                              IconButton(
                                icon: const Icon(Icons.chevron_right),
                                padding: const EdgeInsets.all(4),
                                constraints: const BoxConstraints(),
                                onPressed: _currentPage + 5 < _totalPages
                                    ? () {
                                  setState(() {
                                    if (_currentPage % 5 == 4) {
                                      _currentPage = (_currentPage + 5).clamp(0, _totalPages - 1); // 5단위로 이동
                                    } else {
                                      _currentPage++; // 1단위로 이동
                                    }
                                  });
                                  _fetchItems();
                                }
                                    : null,
                              ),
                            ],
                          ),
                        ),
                    ],
                  );
                }
              },
            )
          ],
        ),
      ),
    );
  }
}