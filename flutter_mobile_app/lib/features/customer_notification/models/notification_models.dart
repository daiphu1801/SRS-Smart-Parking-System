/// Model cho một thông báo hệ thống gửi đến khách hàng.
/// Ánh xạ từ bảng Notifications trong database.
class AppNotification {
  final int id;
  final String title;
  final String body;
  final NotificationType type;
  final bool isRead;
  final DateTime createdAt;

  const AppNotification({
    required this.id,
    required this.title,
    required this.body,
    required this.type,
    required this.isRead,
    required this.createdAt,
  });

  factory AppNotification.fromJson(Map<String, dynamic> json) {
    final typeStr = (json['type'] as String?)?.toUpperCase();
    var type = NotificationType.system;
    
    if (typeStr == 'BROADCAST_ALERT') {
      type = NotificationType.broadcast;
    } else {
      type = NotificationType.values.firstWhere(
        (e) => e.name.toUpperCase() == typeStr,
        orElse: () => NotificationType.system,
      );
    }

    return AppNotification(
      id: json['id'] as int,
      title: json['title'] as String,
      body: json['content'] as String,
      type: type,
      isRead: json['isRead'] as bool? ?? false,
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'])
          : DateTime.now(),
    );
  }

  AppNotification copyWith({bool? isRead}) => AppNotification(
        id: id,
        title: title,
        body: body,
        type: type,
        isRead: isRead ?? this.isRead,
        createdAt: createdAt,
      );
}

enum NotificationType {
  /// Nhắc nợ / sắp hết hạn gói cước
  debt,

  /// Cảnh báo an ninh (đỗ sai vị trí...)
  security,

  /// Thông báo hệ thống chung
  system,
  
  /// Thông báo phát thanh (Broadcast)
  broadcast,
}
