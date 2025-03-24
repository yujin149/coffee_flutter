
enum BoardStatus {
  NOTICE,
  GENERAL;

  String get description {
    switch (this) {
      case BoardStatus.NOTICE:
        return '공지';
      case BoardStatus.GENERAL:
        return '일반';
    }
  }
}

class BoardItem {
  final int number;
  final String title;
  final String regDate;
  final BoardStatus boardStatus;

  BoardItem({
    required this.number,
    required this.title,
    required this.regDate,
    required this.boardStatus,
  });

  factory BoardItem.fromJson(Map<String, dynamic> json) {
    return BoardItem(
      number: json['id'] as int,
      title: json['title'] as String,
      regDate: json['regTime'].toString().split('T')[0],
      boardStatus: BoardStatus.values.firstWhere(
        (e) => e.name == json['boardStatus'],
        orElse: () => BoardStatus.GENERAL,
      ),
    );
  }
} 