import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_home/models/home_models.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';

class WelcomeBanner extends StatelessWidget {
  final String fullName;
  const WelcomeBanner({super.key, required this.fullName});

  @override
  Widget build(BuildContext context) {
    final hour = DateTime.now().hour;
    final greeting = hour < 12 ? 'Chào buổi sáng' : (hour < 18 ? 'Chào buổi chiều' : 'Chào buổi tối');
    
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [AppTheme.primary, Color(0xFF104e2a)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: AppTheme.primary.withValues(alpha: 0.3),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '$greeting,',
                  style: AppTheme.bodySmall.copyWith(color: Colors.white70),
                ),
                const SizedBox(height: 4),
                Text(
                  '${fullName.split(' ').last} 👋',
                  style: AppTheme.heading2.copyWith(color: Colors.white),
                ),
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.verified_user_outlined, size: 14, color: Colors.white),
                      const SizedBox(width: 6),
                      Text('Tài khoản đã xác thực', style: AppTheme.caption.copyWith(color: Colors.white)),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const Icon(Icons.directions_car, size: 56, color: Colors.white24),
        ],
      ),
    );
  }
}

class AnnouncementCarousel extends StatelessWidget {
  const AnnouncementCarousel({super.key});

  @override
  Widget build(BuildContext context) {
    final announcements = [
      {
        'title': 'Bảo trì bãi đỗ xe Khu A',
        'desc': 'Khu A sẽ bảo trì từ 22:00 hôm nay. Vui lòng di chuyển xe sang Khu B.',
        'color': Colors.orange.shade50,
        'iconColor': Colors.orange.shade700,
        'icon': Icons.construction,
      },
      {
        'title': 'Gia hạn gói cước',
        'desc': 'Gói cước của bạn sẽ tự động gia hạn vào cuối tháng. Kiểm tra ngay!',
        'color': Colors.blue.shade50,
        'iconColor': Colors.blue.shade700,
        'icon': Icons.card_membership_outlined,
      },
    ];

    return SizedBox(
      height: 90,
      child: PageView.builder(
        controller: PageController(viewportFraction: 0.95),
        itemCount: announcements.length,
        itemBuilder: (context, index) {
          final item = announcements[index];
          return Container(
            margin: const EdgeInsets.only(right: 12),
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: item['color'] as Color,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: (item['iconColor'] as Color).withValues(alpha: 0.3)),
            ),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: const BoxDecoration(
                    color: Colors.white,
                    shape: BoxShape.circle,
                  ),
                  child: Icon(item['icon'] as IconData, color: item['iconColor'] as Color),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(item['title'] as String, style: AppTheme.label),
                      const SizedBox(height: 4),
                      Text(
                        item['desc'] as String,
                        style: AppTheme.caption,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class VehicleSection extends StatelessWidget {
  final ViewState<List<VehicleInfo>> state;
  const VehicleSection({super.key, required this.state});

  @override
  Widget build(BuildContext context) {
    return switch (state) {
      Loading() => const SizedBox(height: 116, child: Center(child: CircularProgressIndicator())),
      Success(:final data) => SizedBox(
          height: 116,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: data.length,
            separatorBuilder: (_, __) => const SizedBox(width: 12),
            itemBuilder: (_, i) => VehicleCard(vehicle: data[i]),
          ),
        ),
      Failure(:final message) => AppEmptyState(icon: Icons.error_outline, title: message),
      Idle() => const SizedBox.shrink(),
    };
  }
}

class SessionSection extends StatelessWidget {
  final ViewState<ActiveSession?> state;
  const SessionSection({super.key, required this.state});

  @override
  Widget build(BuildContext context) {
    return switch (state) {
      Loading() => const AppCard(child: SizedBox(height: 60, child: Center(child: CircularProgressIndicator()))),
      Success(:final data) when data != null => AppCard(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(data.plate, style: AppTheme.heading3),
              const SizedBox(height: 4),
              Row(
                children: [
                  Icon(Icons.login, size: 14, color: AppTheme.subtle),
                  const SizedBox(width: 4),
                  Text('Vào lúc: ${data.enteredAt}',
                      style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                ],
              ),
              const SizedBox(height: 4),
              Row(
                children: [
                  Icon(Icons.timer_outlined, size: 14, color: AppTheme.subtle),
                  const SizedBox(width: 4),
                  Text('Thời gian đỗ: ${data.duration}',
                      style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                ],
              ),
            ],
          ),
        ),
      Success() => AppEmptyState(
          icon: Icons.local_parking_outlined,
          title: 'Không có phiên đỗ xe',
          subtitle: 'Bạn chưa vào bãi đỗ xe nào.',
        ),
      Failure(:final message) => AppEmptyState(icon: Icons.error_outline, title: message),
      Idle() => const SizedBox.shrink(),
    };
  }
}

class QuickActions extends StatelessWidget {
  const QuickActions({super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: ActionCard(
            icon: Icons.local_parking_outlined,
            label: 'Phiên đỗ xe',
            onTap: () => context.push(AppRoutes.parkingSessionList),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: ActionCard(
            icon: Icons.card_membership_outlined,
            label: 'Hóa đơn',
            onTap: () => context.go(AppRoutes.customerPackages),
          ),
        ),
      ],
    );
  }
}

class VehicleCard extends StatelessWidget {
  final VehicleInfo vehicle;
  const VehicleCard({super.key, required this.vehicle});

  @override
  Widget build(BuildContext context) {
    return AppCard(
      padding: const EdgeInsets.all(14),
      child: SizedBox(
        width: 160,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(vehicle.plate, style: AppTheme.heading3.copyWith(fontSize: 15)),
                AppBadge(
                  label: vehicle.isExpired ? 'Hết hạn' : 'Còn hạn',
                  isFilled: !vehicle.isExpired,
                ),
              ],
            ),
            Text(vehicle.packageName,
                style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
            Text(
              vehicle.isExpired ? 'Đã hết hạn' : 'Còn ${vehicle.daysLeft} ngày',
              style: AppTheme.bodySmall.copyWith(
                color: vehicle.isExpired ? AppTheme.disabled : AppTheme.primary,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class ActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  const ActionCard({super.key, required this.icon, required this.label, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return AppCard(
      onTap: onTap,
      padding: const EdgeInsets.all(14),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, size: 24, color: AppTheme.primary),
          const SizedBox(height: 8),
          Text(label, style: AppTheme.label, textAlign: TextAlign.center),
        ],
      ),
    );
  }
}
