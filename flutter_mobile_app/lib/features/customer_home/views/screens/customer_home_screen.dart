import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_home/viewmodels/home_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_notification/viewmodels/notification_viewmodel.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/features/customer_home/views/widgets/home_widgets.dart';

/// View: Customer Home Dashboard
/// ViewModel: HomeViewModel (data), AuthViewModel (logout)
class CustomerHomeScreen extends StatelessWidget {
  const CustomerHomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => HomeViewModel(),
      child: const _CustomerHomeView(),
    );
  }
}

class _CustomerHomeView extends StatelessWidget {
  const _CustomerHomeView();

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HomeViewModel>();
    final authVm = context.read<AuthViewModel>();
    final profile = authVm.profile;

    return Scaffold(
      appBar: AppBar(
        title: const Icon(Icons.local_parking_rounded, size: 28),
        centerTitle: false,
        actions: [
          Consumer<NotificationViewModel>(
            builder: (_, notifVm, __) {
              return IconButton(
                icon: notifVm.hasUnread
                    ? Badge(
                        backgroundColor: Colors.red,
                        label: Text(
                          notifVm.unreadCount.toString(),
                          style: const TextStyle(fontSize: 10),
                        ),
                        child: const Icon(Icons.notifications_outlined),
                      )
                    : const Icon(Icons.notifications_outlined),
                onPressed: () => context.push(AppRoutes.notificationList),
              );
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => context.read<HomeViewModel>().loadData(),
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(AppTheme.pagePadding),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // ── Greeting Banner ─────────────────────────────────────
              if (profile != null) ...[
                WelcomeBanner(fullName: profile.fullName),
                const SizedBox(height: 24),
              ],

              // ── Announcements Carousel ──────────────────────────────
              const AnnouncementCarousel(),
              const SizedBox(height: AppTheme.sectionGap),

              // ── Vehicles ──────────────────────────────────────
              Text('Phương tiện của bạn', style: AppTheme.heading3),
              const SizedBox(height: 12),
              VehicleSection(state: vm.vehicleState),

              // ── Active Session ────────────────────────────────
              const SizedBox(height: AppTheme.sectionGap),
              Text('Phiên đỗ xe hiện tại', style: AppTheme.heading3),
              const SizedBox(height: 12),
              SessionSection(state: vm.sessionState),

              // ── Quick Actions ─────────────────────────────────
              const SizedBox(height: AppTheme.sectionGap),
              Text('Thao tác nhanh', style: AppTheme.heading3),
              const SizedBox(height: 12),
              const QuickActions(),
            ],
          ),
        ),
      ),
    );
  }
}
