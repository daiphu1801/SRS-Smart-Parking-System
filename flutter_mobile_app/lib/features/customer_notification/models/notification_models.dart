/// Model cho một thông báo hệ thống gửi đến khách hàng.
/// Ánh xạ từ bảng Notifications trong database.
class AppNotification {
  final String id;
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
}

extension NotificationTypeInfo on NotificationType {
  String get label {
    switch (this) {
      case NotificationType.debt:
        return 'Nhắc nợ';
      case NotificationType.security:
        return 'Cảnh báo an ninh';
      case NotificationType.system:
        return 'Hệ thống';
    }
  }
}
