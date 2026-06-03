import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_notification/models/notification_models.dart';
import 'package:smart_parking_mobile/features/customer_notification/viewmodels/notification_viewmodel.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

/// Màn hình Danh sách Thông báo — theo chuẩn MVVM.
/// Danh sách cuộn dọc đơn giản với badge đọc/chưa đọc.
class NotificationListScreen extends StatefulWidget {
  const NotificationListScreen({super.key});

  @override
  State<NotificationListScreen> createState() => _NotificationListScreenState();
}

class _NotificationListScreenState extends State<NotificationListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final vm = context.read<NotificationViewModel>();
      vm.clearBadge();
      vm.fetchNotifications(customerId: 'CUST-001');
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.notifications, style: AppTheme.heading1),
        actions: [
          Consumer<NotificationViewModel>(
            builder: (_, vm, __) {
              if (!vm.hasUnread) return const SizedBox.shrink();
              return TextButton(
                onPressed: vm.markAllAsRead,
                child: Text(
                  l10n.readAll,
                  style: AppTheme.label.copyWith(color: AppTheme.primary),
                ),
              );
            },
          ),
        ],
      ),
      body: Consumer<NotificationViewModel>(
        builder: (context, vm, _) {
          final state = vm.notificationState;

          if (state is Loading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is Failure) {
            return AppEmptyState(
              icon: Icons.error_outline,
              title: l10n.cannotLoadNotifications,
              subtitle: (state as Failure).message,
            );
          }

          if (state is Success<List<AppNotification>>) {
            final notifications = state.data;

            if (notifications.isEmpty) {
              return AppEmptyState(
                icon: Icons.notifications_none_outlined,
                title: l10n.noNotificationsYet,
                subtitle: l10n.systemNotificationsAppearHere,
              );
            }

            return RefreshIndicator(
              color: AppTheme.primary,
              onRefresh: () => vm.fetchNotifications(customerId: 'CUST-001'),
              child: ListView.builder(
                padding: const EdgeInsets.symmetric(vertical: 8),
                itemCount: notifications.length,
                itemBuilder: (context, i) => _NotificationCard(
                  notification: notifications[i],
                  onTap: () => vm.markAsRead(notifications[i].id),
                ),
              ),
            );
          }

          return const SizedBox.shrink();
        },
      ),
    );
  }
}

// ── Notification Card ─────────────────────────────────────────────────────────

class _NotificationCard extends StatelessWidget {
  final AppNotification notification;
  final VoidCallback onTap;

  const _NotificationCard({
    required this.notification,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final timeFormatter = DateFormat('HH:mm · dd/MM/yyyy');

    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(
            horizontal: AppTheme.pagePadding, vertical: 14),
        decoration: BoxDecoration(
          color: notification.isRead
              ? Colors.transparent
              : AppTheme.primary.withValues(alpha: 0.05),
          border: Border(
            bottom: BorderSide(color: AppTheme.border, width: 0.5),
          ),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Type icon
            Container(
              margin: const EdgeInsets.only(top: 2),
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: _typeColor(notification.type).withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(
                _typeIcon(notification.type),
                color: _typeColor(notification.type),
                size: 18,
              ),
            ),
            const SizedBox(width: 12),

            // Content
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          notification.type == NotificationType.broadcast 
                              ? '[Phát thanh] ${notification.title}' 
                              : notification.title,
                          style: AppTheme.label.copyWith(
                            color: notification.type == NotificationType.broadcast ? Colors.deepPurple : null,
                            fontWeight: notification.isRead
                                ? FontWeight.w500
                                : FontWeight.w700,
                          ),
                        ),
                      ),
                      if (!notification.isRead)
                        Container(
                          width: 8,
                          height: 8,
                          margin: const EdgeInsets.only(left: 8),
                          decoration: const BoxDecoration(
                            color: AppTheme.primary,
                            shape: BoxShape.circle,
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    notification.body,
                    style: AppTheme.bodySmall.copyWith(
                        color: AppTheme.subtle, height: 1.4),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 6),
                  Text(
                    timeFormatter.format(notification.createdAt),
                    style: AppTheme.caption.copyWith(color: AppTheme.disabled),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  IconData _typeIcon(NotificationType type) {
    switch (type) {
      case NotificationType.debt:
        return Icons.notifications_active_outlined;
      case NotificationType.security:
        return Icons.security_outlined;
      case NotificationType.system:
        return Icons.info_outline_rounded;
      case NotificationType.broadcast:
        return Icons.campaign_rounded;
    }
  }

  Color _typeColor(NotificationType type) {
    switch (type) {
      case NotificationType.debt:
        return Colors.orange;
      case NotificationType.security:
        return Colors.red;
      case NotificationType.system:
        return AppTheme.primary;
      case NotificationType.broadcast:
        return Colors.deepPurple;
    }
  }
}
