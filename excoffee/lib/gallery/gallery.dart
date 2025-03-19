import 'package:flutter/material.dart';
import 'gallery_service.dart';
import 'gallery_model.dart';
import 'detail.dart';

class Gallery extends StatefulWidget {
  const Gallery({super.key});

  @override
  State<Gallery> createState() => _GalleryState();
}

class _GalleryState extends State<Gallery> {
  final GalleryService _galleryService = GalleryService();
  List<GalleryItem> _ongoingGalleries = [];
  List<GalleryItem> _endedGalleries = [];
  bool _isLoading = false;
  int _currentPage = 0;
  bool _hasMore = true;
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';
  String _searchType = 'all';
  final ScrollController _scrollController = ScrollController();
  bool _showScrollToTop = false;

  @override
  void initState() {
    super.initState();
    _loadGalleries();
    _scrollController.addListener(_onScroll); //스크롤 리스너 추가
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  //스크롤이 200픽셀 전에 이벤트 로드
  void _onScroll() {
    if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent - 200) {
      if (!_isLoading && _hasMore) {
        _loadGalleries();
      }
    }

//스크롤 탑 버튼 표시
    setState(() {
      _showScrollToTop = _scrollController.position.pixels > 300;
    });
  }

  void _scrollToTop() {
    _scrollController.animateTo(
      0,
      duration: const Duration(milliseconds: 500),
      curve: Curves.easeInOut,
    );
  }

  Future<void> _loadGalleries() async {
    if (!mounted) return;

    setState(() {
      _isLoading = true;
    });

    try {
      final response = await _galleryService.getGalleries(
        page: _currentPage,
        searchQuery: _searchQuery,
        searchType: _searchType,
      );

      if (!mounted) return;

      final List<GalleryItem> galleries = response['galleries'];
      final bool hasMore = response['hasMore'];

      setState(() {
        if (_currentPage == 0) {
          _ongoingGalleries = galleries.where((g) => g.galleryStatus == 'ON').toList();
          _endedGalleries = galleries.where((g) => g.galleryStatus == 'OFF').toList();
        } else {
          _ongoingGalleries.addAll(galleries.where((g) => g.galleryStatus == 'ON'));
          _endedGalleries.addAll(galleries.where((g) => g.galleryStatus == 'OFF'));
        }
        _hasMore = hasMore;
        _currentPage++;
        _isLoading = false;
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isLoading = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString())),
      );
    }
  }

  void _resetAndSearch() {
    setState(() {
      _currentPage = 0;
      _ongoingGalleries = [];
      _endedGalleries = [];
      _hasMore = true;
    });
    _loadGalleries();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text(
          '이벤트',
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        ),
        backgroundColor: const Color(0xFFEE3424),
        foregroundColor: Colors.white,
      ),
      //스크롤 탑 버튼 표시
      floatingActionButton: _showScrollToTop
          ? FloatingActionButton(
              onPressed: _scrollToTop,
              backgroundColor: const Color(0xFFEE3424),
              child: const Icon(Icons.arrow_upward, color: Colors.white),
              mini: true,
            )
          : null,
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
                    padding: const EdgeInsets.symmetric(horizontal: 8),
                    decoration: BoxDecoration(
                      border: Border.all(color: const Color(0xFFDCDCDC)),
                      borderRadius: BorderRadius.circular(5),
                    ),
                    child: Center(
                      child: DropdownButton<String>(
                        value: _searchType,
                        underline: const SizedBox(),
                        isDense: true,
                        items: const [
                          DropdownMenuItem(
                            value: 'all',
                            child: Text(
                              '전체',
                              style: TextStyle(color: Color(0xFF606060)),
                            ),
                          ),
                          DropdownMenuItem(
                            value: 'title',
                            child: Text(
                              '제목',
                              style: TextStyle(color: Color(0xFF606060)),
                            ),
                          ),
                          DropdownMenuItem(
                            value: 'content',
                            child: Text(
                              '내용',
                              style: TextStyle(color: Color(0xFF606060)),
                            ),
                          ),
                        ],
                        onChanged: (value) {
                          if (value != null) {
                            setState(() {
                              _searchType = value;
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
                      style: const TextStyle(color: Color(0xFF606060)),
                      decoration: InputDecoration(
                        hintText: '검색어를 입력하세요',
                        hintStyle: const TextStyle(color: Color(0xFF888888)),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(5),
                          borderSide: const BorderSide(color: Color(0xFFDCDCDC)),
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(5),
                          borderSide: const BorderSide(color: Color(0xFFDCDCDC)),
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(5),
                          borderSide: const BorderSide(color: Color(0xFFDCDCDC)),
                        ),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 12,
                        ),
                      ),
                      onSubmitted: (value) {
                        setState(() {
                          _searchQuery = value;
                        });
                        _resetAndSearch();
                      },
                    ),
                  ),
                  const SizedBox(width: 8),
                  // 검색 버튼
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size(0, 48),
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      backgroundColor: const Color(0xFFEE3424),
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(5),
                      ),
                    ),
                    onPressed: () {
                      setState(() {
                        _searchQuery = _searchController.text;
                      });
                      _resetAndSearch();
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
          // 갤러리 목록
          Expanded(
            child: _isLoading && _ongoingGalleries.isEmpty && _endedGalleries.isEmpty
                ? const Center(child: CircularProgressIndicator())
                : SingleChildScrollView(
                    controller: _scrollController,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        if (_ongoingGalleries.isNotEmpty) ...[
                          const Padding(
                            padding: const EdgeInsets.only(
                              top: 16,
                              left: 16,
                              right: 16,
                              bottom: 5,
                            ),
                            child: Text(
                              '진행중인 이벤트',
                              style: TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          GridView.builder(
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            padding: const EdgeInsets.only(
                              top: 0,
                              left: 16,
                              right: 16,
                              bottom: 16,
                            ),
                            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                              crossAxisCount: 1,
                              childAspectRatio: 1.5,
                              crossAxisSpacing: 5,
                              //mainAxisSpacing: 16,
                            ),
                            itemCount: _ongoingGalleries.length,
                            itemBuilder: (context, index) {
                              final gallery = _ongoingGalleries[index];
                              return _buildGalleryCard(gallery, true);
                            },
                          ),
                        ],
                        if (_endedGalleries.isNotEmpty) ...[
                          const Padding(
                            padding: EdgeInsets.all(16.0),
                            child: Text(
                              '종료된 이벤트',
                              style: TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          GridView.builder(
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            padding: const EdgeInsets.only(
                              top: 0,
                              left: 16,
                              right: 16,
                              bottom: 16,
                            ),
                            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                              crossAxisCount: 1,
                              childAspectRatio: 1.5,
                              //crossAxisSpacing: 5,
                              mainAxisSpacing: 16,
                            ),
                            itemCount: _endedGalleries.length,
                            itemBuilder: (context, index) {
                              final gallery = _endedGalleries[index];
                              return _buildGalleryCard(gallery, false);
                            },
                          ),
                        ],
                        if (_isLoading && (_ongoingGalleries.isNotEmpty || _endedGalleries.isNotEmpty))
                          const Padding(
                            padding: EdgeInsets.all(16.0),
                            child: Center(child: CircularProgressIndicator()),
                          ),
                      ],
                    ),
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildGalleryCard(GalleryItem gallery, bool isOngoing) {
    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => GalleryDetail(gallery: gallery),
          ),
        );
      },
      child: Card(
        elevation: 0,
        color: Colors.white,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
          side: const BorderSide(
            color: Color(0xFFDCDCDC),
            width: 1.0,
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: ClipRRect(
                borderRadius: const BorderRadius.vertical(top: Radius.circular(8)),
                child: Image.network(
                  gallery.repImgUrl,
                  fit: BoxFit.cover,
                  width: double.infinity,
                  errorBuilder: (context, error, stackTrace) {
                    return Container(
                      color: Colors.grey[300],
                      child: const Icon(Icons.error),
                    );
                  },
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(12.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 8,
                          vertical: 3,
                        ),
                        decoration: BoxDecoration(
                          color: isOngoing
                              ? const Color(0xFF4CAF50)
                              : Colors.grey[400],
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          isOngoing ? '진행중' : '종료',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          gallery.title,
                          style: const TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.bold,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${gallery.startTime} ~ ${gallery.endTime}',
                    style: TextStyle(
                      fontSize: 13,
                      color: Colors.grey[600],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
