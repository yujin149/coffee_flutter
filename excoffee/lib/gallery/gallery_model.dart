class GalleryItem {
  final int id;
  final String title;
  final String repImgUrl;
  final String galleryStatus;
  final String startTime;
  final String endTime;
  final List<String> galleryImgList;
  final String content;

  GalleryItem({
    required this.id,
    required this.title,
    required this.repImgUrl,
    required this.galleryStatus,
    required this.startTime,
    required this.endTime,
    required this.galleryImgList,
    required this.content,
  });

  factory GalleryItem.fromJson(Map<String, dynamic> json) {
    return GalleryItem(
      id: json['id'],
      title: json['title'],
      repImgUrl: json['repImgUrl'],
      galleryStatus: json['galleryStatus'],
      startTime: json['startTime'],
      endTime: json['endTime'],
      galleryImgList: List<String>.from(json['galleryImgList'] ?? []),
      content: json['content'] ?? '',
    );
  }
} 