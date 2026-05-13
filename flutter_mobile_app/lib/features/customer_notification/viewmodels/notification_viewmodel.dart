import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_notification/models/notification_models.dart';

/// NotificationViewModel: quản lý danh sách thông báo hệ thống của khách hàng.
/// Cung cấp mock data — sau này thay bằng repository gọi API.
class NotificationViewModel extends ChangeNotifier {
  ViewState<List<AppNotification>> notificationState = const Idle();

  // ── MOCK DATA ──────────────────────────────────────────────────────────────
  final List<AppNotification> _allNotifications = [
    AppNotification(
      id: 'NOTIF-001',
      title: 'Gói cước sắp hết hạn',
      body: 'Gói cước xe 30A-123.45 sẽ hết hạn trong 3 ngày (08/05/2026). Vui lòng gia hạn để tiếp tục sử dụng.',
      type: NotificationType.debt,
      isRead: false,
      createdAt: DateTime.now().subtract(const Duration(hours: 1)),
    ),
    AppNotification(
      id: 'NOTIF-002',
      title: 'Cảnh báo an ninh',
      body: 'Xe 30A-123.45 đã được phát hiện đỗ không đúng vị trí quy định tại Tòa A lúc 14:23. Bảo vệ đã được thông báo.',
      type: NotificationType.security,
      isRead: false,
      createdAt: DateTime.now().subtract(const Duration(hours: 3)),
    ),
    AppNotification(
      id: 'NOTIF-003',
      title: 'Thanh toán thành công',
      body: 'Giao dịch thanh toán 25.000đ cho phiên đỗ xe PS-2002 đã được xác nhận thành công.',
      type: NotificationType.system,
      isRead: true,
      createdAt: DateTime.now().subtract(const Duration(days: 1, hours: 2)),
    ),
    AppNotification(
      id: 'NOTIF-004',
      title: 'Nhắc nợ hóa đơn',
      body: 'Hóa đơn đỗ xe PS-2003 trị giá 50.000đ chưa được thanh toán. Vui lòng thanh toán trong 24 giờ.',
      type: NotificationType.debt,
      isRead: true,
      createdAt: DateTime.now().subtract(const Duration(days: 2)),
    ),
    AppNotification(
      id: 'NOTIF-005',
      title: 'Gia hạn gói cước thành công',
      body: 'Gói cước xe 30A-123.45 đã được gia hạn thêm 1 tháng. Thời hạn mới: 05/06/2026.',
      type: NotificationType.system,
      isRead: true,
      createdAt: DateTime.now().subtract(const Duration(days: 5)),
    ),
  ];

  // ── Derived State ──────────────────────────────────────────────────────────
  int get unreadCount {
    if (notificationState is! Success<List<AppNotification>>) return 0;
    final list = (notificationState as Success<List<AppNotification>>).data;
    return list.where((n) => !n.isRead).length;
  }

  bool get hasUnread => unreadCount > 0;

  // ── Actions ────────────────────────────────────────────────────────────────

  /// Tải danh sách thông báo theo Customer ID.
  Future<void> fetchNotifications({required String customerId}) async {
    notificationState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 500));
      final sorted = [..._allNotifications]
        ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
      notificationState = Success(sorted);
    } catch (e) {
      notificationState = Failure(e.toString());
    }
    notifyListeners();
  }

  /// Đánh dấu một thông báo đã đọc theo ID.
  void markAsRead(String id) {
    if (notificationState is! Success<List<AppNotification>>) return;
    final current = (notificationState as Success<List<AppNotification>>).data;
    final updated = current
        .map((n) => n.id == id ? n.copyWith(isRead: true) : n)
        .toList();

    // Đồng bộ lại _allNotifications
    final idx = _allNotifications.indexWhere((n) => n.id == id);
    if (idx != -1) {
      _allNotifications[idx] = _allNotifications[idx].copyWith(isRead: true);
    }

    notificationState = Success(updated);
    notifyListeners();
  }

  /// Đánh dấu tất cả thông báo đã đọc.
  void markAllAsRead() {
    if (notificationState is! Success<List<AppNotification>>) return;
    final current = (notificationState as Success<List<AppNotification>>).data;
    final updated = current.map((n) => n.copyWith(isRead: true)).toList();

    for (var i = 0; i < _allNotifications.length; i++) {
      _allNotifications[i] = _allNotifications[i].copyWith(isRead: true);
    }

    notificationState = Success(updated);
    notifyListeners();
  }
}
