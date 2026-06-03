import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_notification/models/notification_models.dart';
import 'package:smart_parking_mobile/features/customer_notification/services/notification_api_service.dart';
import 'package:smart_parking_mobile/core/utils/local_storage.dart' as app_storage;

class NotificationViewModel extends ChangeNotifier {
  final NotificationApiService _apiService;
  ViewState<List<AppNotification>> notificationState = const Idle();
  RealtimeChannel? _subscription;
  final Set<int> _readIds = {}; // Temporarily store read IDs locally

  int _realtimeUnreadCount = 0;

  NotificationViewModel(this._apiService);

  // ── Derived State ──────────────────────────────────────────────────────────
  int get unreadCount => _realtimeUnreadCount;

  bool get hasUnread => _realtimeUnreadCount > 0;

  void clearBadge() {
    if (_realtimeUnreadCount > 0) {
      _realtimeUnreadCount = 0;
      notifyListeners();
    }
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  Future<void> fetchNotifications({required String customerId}) async {
    notificationState = const Loading();
    notifyListeners();

    try {
      final notifications = await _apiService.getNotifications();
      // Apply local read state
      for (var i = 0; i < notifications.length; i++) {
        if (_readIds.contains(notifications[i].id)) {
          notifications[i] = notifications[i].copyWith(isRead: true);
        }
      }
      // Sort newest first
      notifications.sort((a, b) => b.createdAt.compareTo(a.createdAt));
      notificationState = Success(notifications);
    } catch (e) {
      notificationState = Failure(e.toString());
    }
    notifyListeners();
  }

  void listenToRealtimeNotifications(int accountId, Function(AppNotification) onNewNotification) async {
    print('🔔 listenToRealtimeNotifications called with accountId: $accountId');
    if (_subscription != null) {
      print('🔔 Already listening, returning early.');
      return;
    }

    print('🔔 Initializing Supabase Realtime channel...');
    try {
      final app_storage.LocalStorage storage = app_storage.LocalStorage.instance;
      final accessToken = await storage.getToken();
      if (accessToken != null && accessToken.isNotEmpty) {
        Supabase.instance.client.realtime.setAuth(accessToken);
        print('🔔 Realtime authenticated with JWT!');
      }
    } catch (e) {
      print('🔔 Failed to set realtime auth: $e');
    }

    _subscription = Supabase.instance.client.channel('public:notifications');
    _subscription!.onPostgresChanges(
      event: PostgresChangeEvent.insert,
      schema: 'public',
      table: 'notifications',
      callback: (payload) {
        print('🔔 Received PostgresChangeEvent.insert payload: ${payload.newRecord}');
        final newRow = payload.newRecord;
        if (newRow['account_id'] == accountId || newRow['type'] == 'BROADCAST_ALERT') {
          var newNotification = AppNotification.fromJson(newRow);
          
          if (_readIds.contains(newNotification.id)) {
             newNotification = newNotification.copyWith(isRead: true);
          }

          if (notificationState is Success<List<AppNotification>>) {
            final currentList = (notificationState as Success<List<AppNotification>>).data;
            final updatedList = [newNotification, ...currentList];
            notificationState = Success(updatedList);
          }
          
          _realtimeUnreadCount++;
          notifyListeners();
          
          onNewNotification(newNotification);
        }
      },
    ).subscribe();
  }

  @override
  void dispose() {
    _subscription?.unsubscribe();
    super.dispose();
  }

  void markAsRead(int id) {
    _readIds.add(id); // Save locally
    if (notificationState is! Success<List<AppNotification>>) return;
    final current = (notificationState as Success<List<AppNotification>>).data;
    final updated = current
        .map((n) => n.id == id ? n.copyWith(isRead: true) : n)
        .toList();

    notificationState = Success(updated);
    notifyListeners();
  }

  void markAllAsRead() {
    if (notificationState is! Success<List<AppNotification>>) return;
    final current = (notificationState as Success<List<AppNotification>>).data;
    final updated = current.map((n) {
      _readIds.add(n.id); // Save locally
      return n.copyWith(isRead: true);
    }).toList();

    notificationState = Success(updated);
    notifyListeners();
  }
}

