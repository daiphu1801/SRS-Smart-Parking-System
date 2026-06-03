import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/di/service_locator.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/utils/language_viewmodel.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_home/models/home_models.dart';
import 'package:smart_parking_mobile/features/customer_home/repositories/home_repository.dart';
import 'package:smart_parking_mobile/features/customer_home/viewmodels/home_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_notification/viewmodels/notification_viewmodel.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/features/customer_home/views/widgets/home_widgets.dart';

/// View: Customer Home Dashboard
class CustomerHomeScreen extends StatelessWidget {
  const CustomerHomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => HomeViewModel(repository: sl<HomeRepository>())..loadData(),
      child: const _CustomerHomeView(),
    );
  }
}

class _CustomerHomeView extends StatefulWidget {
  const _CustomerHomeView();

  @override
  State<_CustomerHomeView> createState() => _CustomerHomeViewState();
}

class _CustomerHomeViewState extends State<_CustomerHomeView> {
  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HomeViewModel>();
    final notifVm = context.read<NotificationViewModel>();

    print('🏠 CustomerHomeScreen build: state is ${vm.state.runtimeType}');

    // Listen to realtime notifications once home data is loaded
    if (vm.state is Success<HomeDashboard>) {
      final accountId = (vm.state as Success<HomeDashboard>).data.profile.customerId;
      print('🏠 HomeDashboard loaded! accountId = $accountId');
      if (accountId > 0) {
        notifVm.listenToRealtimeNotifications(accountId, (newNotif) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('New Notification: ${newNotif.title}'),
                behavior: SnackBarBehavior.floating,
                backgroundColor: AppTheme.primary,
                duration: const Duration(seconds: 4),
                action: SnackBarAction(
                  label: 'View',
                  textColor: Colors.white,
                  onPressed: () => context.push(AppRoutes.notificationList),
                ),
              ),
            );
          }
        });
      }
    }

    return Scaffold(
      appBar: AppBar(
        title: const Icon(Icons.local_parking_rounded, size: 28),
        centerTitle: false,
        actions: [
          // Elegant Language Switcher
          Consumer<LanguageViewModel>(
            builder: (context, langVm, child) {
              final isVi = langVm.currentLocale.languageCode == 'vi';
              return Padding(
                padding: const EdgeInsets.only(right: 8),
                child: InkWell(
                  onTap: () {
                    final newLocale = isVi ? const Locale('en') : const Locale('vi');
                    langVm.changeLanguage(newLocale);
                  },
                  borderRadius: BorderRadius.circular(20),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppTheme.primary.withValues(alpha: 0.05),
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(
                        color: AppTheme.primary.withValues(alpha: 0.15),
                        width: 1,
                      ),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          isVi ? '🇻🇳 VI' : '🇬🇧 EN',
                          style: AppTheme.bodySmall.copyWith(
                            fontWeight: FontWeight.w600,
                            color: AppTheme.primary,
                          ),
                        ),
                        const SizedBox(width: 4),
                        const Icon(
                          Icons.translate_rounded,
                          size: 14,
                          color: AppTheme.primary,
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
          Consumer<NotificationViewModel>(
            builder: (context, notifVm, child) {
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
          child: switch (vm.state) {
            Loading() => const HomeLoadingShimmer(),
            Success(:final data) => _HomeContent(dashboard: data),
            Failure(:final message) => _HomeErrorView(message: message),
            Idle() => const SizedBox.shrink(),
          },
        ),
      ),
    );
  }
}

class _HomeContent extends StatelessWidget {
  final HomeDashboard dashboard;
  const _HomeContent({required this.dashboard});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // ── Greeting Banner ─────────────────────────────────────
        WelcomeBanner(profile: dashboard.profile),
        const SizedBox(height: 24),

        // ── Pending Action Banner (Expiring Soon) ───────────────
        if (dashboard.pendingActions.hasRenewalAlert) ...[
          PendingBanner(expiringCount: dashboard.pendingActions.expiringSoonCount),
          const SizedBox(height: AppTheme.sectionGap),
        ],

        // ── Vehicles ───────────────────────────────────────────
        Text(l10n.myVehicles, style: AppTheme.heading3),
        const SizedBox(height: 12),
        VehicleSection(vehicles: dashboard.vehicles),

        // ── Active Session ─────────────────────────────────────
        const SizedBox(height: AppTheme.sectionGap),
        Text(l10n.currentParkingSession, style: AppTheme.heading3),
        const SizedBox(height: 12),
        SessionSection(session: dashboard.activeSession),

        // ── Quick Actions ──────────────────────────────────────
        const SizedBox(height: AppTheme.sectionGap),
        Text(l10n.quickActions, style: AppTheme.heading3),
        const SizedBox(height: 12),
        QuickActions(
          pendingPaymentCount: dashboard.pendingActions.totalPending,
        ),
      ],
    );
  }
}

class _HomeErrorView extends StatelessWidget {
  final String message;
  const _HomeErrorView({required this.message});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        const SizedBox(height: 40),
        const Icon(Icons.error_outline, size: 48, color: AppTheme.error),
        const SizedBox(height: 16),
        Text(
          message,
          textAlign: TextAlign.center,
          style: AppTheme.body.copyWith(color: AppTheme.subtle),
        ),
        const SizedBox(height: 24),
        ElevatedButton(
          onPressed: () => context.read<HomeViewModel>().loadData(),
          child: Text(l10n.tryAgain),
        ),
      ],
    );
  }
}

