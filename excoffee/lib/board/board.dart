import 'package:flutter/material.dart';
import 'board_service.dart';
import 'board_model.dart';

class Board extends StatefulWidget {
  const Board({super.key});

  @override
  State<Board> createState() => _BoardState();
}

class _BoardState extends State<Board> {
  final BoardService _boardService = BoardService();
  List<BoardItem> _boards = [];
  bool _isLoading = false;
  int _currentPage = 0;
  int _totalPages = 1;
  final TextEditingController _searchController =
      TextEditingController();
  String _searchQuery = '';
  String _searchBy = 'title';

  @override
  void initState() {
    super.initState();
    _loadBoards();
  }

  Future<void> _loadBoards() async {
    if (!mounted) return;

    setState(() {
      _isLoading = true;
    });

    try {
      print('Loading boards...');
      final response = await _boardService.getBoards(
        page: _currentPage,
        searchQuery: _searchQuery,
        searchBy: _searchBy,
      );

      if (!mounted) return;

      setState(() {
        _boards = response['boards'];
        _totalPages = response['totalPages'];
        _isLoading = false;
      });

      print('Successfully loaded ${_boards.length} boards');
      print('Total pages: $_totalPages');
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isLoading = false;
      });

      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(e.toString())));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text(
          '공지사항',
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        ),
        backgroundColor: const Color(0xFFEE3424),
        foregroundColor: Colors.white,
      ),
      body: Column(
        children: [
          // 검색 영역
          Container(
            decoration: const BoxDecoration(
              border: Border(
                bottom: BorderSide(
                  color: Color(0xFFDCDCDC),
                  width: 1.0,
                ),
              ),
            ),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                children: [
                  // 검색 조건 선택 드롭다운
                  Container(
                    height: 48,
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                    ),
                    decoration: BoxDecoration(
                      border: Border.all(color: Color(0xFFDCDCDC)),
                      borderRadius: BorderRadius.circular(5),
                    ),
                    child: Center(
                      child: DropdownButton<String>(
                        value: _searchBy,
                        underline: const SizedBox(),
                        isDense: true,
                        items: const [
                          DropdownMenuItem(
                            value: 'title',
                            child: Text(
                              '제목',
                              style: TextStyle(
                                color: Color(0xFF606060),
                              ),
                            ),
                          ),
                          DropdownMenuItem(
                            value: 'content',
                            child: Text(
                              '내용',
                              style: TextStyle(
                                color: Color(0xFF606060),
                              ),
                            ),
                          ),
                          DropdownMenuItem(
                            value: 'writer',
                            child: Text(
                              '작성자',
                              style: TextStyle(
                                color: Color(0xFF606060),
                              ),
                            ),
                          ),
                        ],
                        onChanged: (value) {
                          if (value != null) {
                            setState(() {
                              _searchBy = value;
                            });
                          }
                        },
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  // 검색 입력 필드
                  Expanded(
                    child: TextField(
                      controller: _searchController,
                      style: const TextStyle(
                        color: Color(0xFF606060),
                      ),
                      decoration: InputDecoration(
                        hintText: '검색어를 입력하세요',
                        hintStyle: const TextStyle(
                          color: Color(0xFF888888),
                        ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(5),
                          borderSide: BorderSide(
                            color: Color(0xFFDCDCDC),
                          ),
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(5),
                          borderSide: BorderSide(
                            color: Color(0xFFDCDCDC),
                          ),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(5),
                          borderSide: BorderSide(
                            color: Color(0xFFDCDCDC),
                          ),
                        ),
                        contentPadding: EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 12,
                        ),
                      ),
                      onSubmitted: (value) {
                        setState(() {
                          _searchQuery = value;
                          _currentPage = 0;
                        });
                        _loadBoards();
                      },
                    ),
                  ),
                  const SizedBox(width: 8),
                  // 검색 버튼
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size(0, 48),
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                      ),
                      backgroundColor: const Color(0xFFEE3424),
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(5),
                      ),
                    ),
                    onPressed: () {
                      setState(() {
                        _searchQuery = _searchController.text;
                        _currentPage = 0;
                      });
                      _loadBoards();
                    },
                    child: const Text(
                      '검색',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          // 게시글 목록
          Expanded(
            child:
                _isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : _boards.isEmpty
                    ? const Center(child: Text('게시글이 없습니다.'))
                    : ListView.builder(
                      itemCount: _boards.length,
                      itemBuilder: (context, index) {
                        final board = _boards[index];
                        return Container(
                          decoration: BoxDecoration(
                            color:
                                board.boardStatus ==
                                        BoardStatus.NOTICE
                                    ? const Color(0xFFFFF4F3)
                                    : null,
                            border: const Border(
                              bottom: BorderSide(
                                color: Color(0xFFDCDCDC),
                                width: 1.0,
                              ),
                            ),
                          ),
                          child: ListTile(
                            title: Text(
                              board.title,
                              style: TextStyle(
                                fontWeight:
                                    board.boardStatus ==
                                            BoardStatus.NOTICE
                                        ? FontWeight.bold
                                        : FontWeight.normal,
                                color:
                                    board.boardStatus ==
                                            BoardStatus.NOTICE
                                        ? Color(0xFFEE3424)
                                        : null,
                              ),
                            ),
                            subtitle: Text(
                              board.regDate,
                              style: const TextStyle(
                                fontSize: 13,
                                color: Color(0xFF888888),
                              ),
                            ),
                            onTap: () {
                              Navigator.pushNamed(
                                context,
                                '/board/detail',
                                arguments: board.number,
                              );
                            },
                          ),
                        );
                      },
                    ),
          ),
          // 페이징 컨트롤
          if (!_isLoading && _boards.isNotEmpty)
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
                    onPressed:
                        _currentPage > 0
                            ? () {
                              setState(() {
                                _currentPage--;
                              });
                              _loadBoards();
                            }
                            : null,
                  ),
                  const SizedBox(width: 4),
                  // 페이지 번호 버튼들
                  ...List.generate(_totalPages, (index) {
                    final pageNumber = index + 1;
                    final isCurrentPage =
                        _currentPage + 1 == pageNumber;
                    return Container(
                      width: 32,
                      height: 32,
                      margin: const EdgeInsets.symmetric(
                        horizontal: 2,
                      ),
                      child: TextButton(
                        style: TextButton.styleFrom(
                          backgroundColor:
                              isCurrentPage
                                  ? const Color(0xFFEE3424)
                                  : null,
                          foregroundColor:
                              isCurrentPage
                                  ? Colors.white
                                  : Colors.black,
                          padding: EdgeInsets.zero,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(16),
                          ),
                        ),
                        onPressed: () {
                          setState(() {
                            _currentPage = pageNumber - 1;
                          });
                          _loadBoards();
                        },
                        child: Text(
                          pageNumber.toString(),
                          style: TextStyle(
                            fontSize: 14,
                            fontWeight:
                                isCurrentPage
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
                    onPressed:
                        _currentPage < _totalPages - 1
                            ? () {
                              setState(() {
                                _currentPage++;
                              });
                              _loadBoards();
                            }
                            : null,
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }
}
