import 'package:flutter/material.dart';
import 'board_service.dart';

class BoardDetail extends StatefulWidget {
  const BoardDetail({super.key});

  @override
  State<BoardDetail> createState() => _BoardDetailState();
}

class _BoardDetailState extends State<BoardDetail> {
  final BoardService _boardService = BoardService();
  bool _isLoading = true;
  String _error = '';
  Map<String, dynamic>? _boardDetail;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _loadBoardDetail();
  }

  Future<void> _loadBoardDetail() async {
    final boardNumber = ModalRoute.of(context)?.settings.arguments as int;
    
    try {
      final detail = await _boardService.getBoardDetail(boardNumber);
      if (mounted) {
        setState(() {
          _boardDetail = detail;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e.toString();
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text(
          '공지사항',
          style: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
        backgroundColor: const Color(0xFFEE3424),
        foregroundColor: Colors.white,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error.isNotEmpty
              ? Center(child: Text(_error))
              : SingleChildScrollView(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        width: double.infinity,
                        decoration: const BoxDecoration(
                          border: Border(
                            bottom: BorderSide(
                              color: Color(0xFFDCDCDC),
                              width: 1.0,
                            ),
                          ),
                        ),
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            if (_boardDetail?['boardStatus'] == 'NOTICE')
                              Container(
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 8,
                                  vertical: 4,
                                ),
                                margin: const EdgeInsets.only(bottom: 8),
                                decoration: BoxDecoration(
                                  color: const Color(0xFFFFF4F3),
                                  borderRadius: BorderRadius.circular(4),
                                  border: Border.all(
                                    color: const Color(0xFFEE3424),
                                  ),
                                ),
                                child: const Text(
                                  '공지',
                                  style: TextStyle(
                                    color: Color(0xFFEE3424),
                                    fontSize: 12,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                            Text(
                              _boardDetail?['title'] ?? '',
                              style: const TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                                color: Color(0xFF222222),
                              ),
                            ),
                            const SizedBox(height: 16),
                            Row(
                              children: [
                                Text(
                                  _boardDetail?['writer'] ?? '',
                                  style: const TextStyle(
                                    color: Color(0xFF606060),
                                  ),
                                ),
                                const SizedBox(width: 16),
                                Text(
                                  _boardDetail?['regDate'] ?? '',
                                  style: const TextStyle(
                                    color: Color(0xFF888888),
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Text(
                          _boardDetail?['content'] ?? '',
                          style: const TextStyle(
                            fontSize: 16,
                            color: Color(0xFF222222),
                            height: 1.6,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
    );
  }
}
