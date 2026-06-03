import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_home/models/home_models.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

// ── Shimmer Loading ────────────────────────────────────────────────────────
class HomeLoadingShimmer extends StatelessWidget {
  const HomeLoadingShimmer({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _buildShimmerBlock(height: 100, radius: 16),
        const SizedBox(height: AppTheme.sectionGap),
        _buildShimmerBlock(height: 20, width: 150),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(child: _buildShimmerBlock(height: 116, radius: 8)),
            const SizedBox(width: 12),
            Expanded(child: _buildShimmerBlock(height: 116, radius: 8)),
          ],
        ),
        const SizedBox(height: AppTheme.sectionGap),
        _buildShimmerBlock(height: 20, width: 150),
        const SizedBox(height: 12),
        _buildShimmerBlock(height: 100, radius: 8),
      ],
    );
  }

  Widget _buildShimmerBlock({required double height, double width = double.infinity, double radius = 4}) {
    return Container(
      height: height,
      width: width,
      decoration: BoxDecoration(
        color: AppTheme.disabled.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(radius),
      ),
    );
  }
}

// ── Banners ─────────────────────────────────────────────────────────────
class WelcomeBanner extends StatelessWidget {
  final ProfileSummary profile;
  const WelcomeBanner({super.key, required this.profile});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final hour = DateTime.now().hour;
    final greeting = hour < 12
        ? l10n.welcomeMorning
        : (hour < 18 ? l10n.welcomeAfternoon : l10n.welcomeEvening);
    
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
                  '${profile.fullName.split(' ').last} 👋',
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
                      Text(
                        profile.groupName ?? l10n.accountVerified,
                        style: AppTheme.caption.copyWith(color: Colors.white),
                      ),
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

class PendingBanner extends StatelessWidget {
  final int expiringCount;
  const PendingBanner({super.key, required this.expiringCount});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade300),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: const BoxDecoration(
              color: Colors.white,
              shape: BoxShape.circle,
            ),
            child: Icon(Icons.warning_amber_rounded, color: Colors.orange.shade700),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(l10n.expiringSoonAlert, style: AppTheme.label),
                const SizedBox(height: 2),
                Text(
                  l10n.expiringSoonMessage(expiringCount),
                  style: AppTheme.caption,
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          Icon(Icons.chevron_right, color: Colors.orange.shade700),
        ],
      ),
    );
  }
}

// ── Vehicles ────────────────────────────────────────────────────────────
class VehicleSection extends StatelessWidget {
  final List<VehicleCard> vehicles;
  const VehicleSection({super.key, required this.vehicles});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    if (vehicles.isEmpty) {
      return AppEmptyState(
        icon: Icons.directions_car_filled_outlined,
        title: l10n.noVehiclesHome,
        subtitle: l10n.noVehiclesRegistered,
      );
    }

    return SizedBox(
      height: 120,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: vehicles.length,
        separatorBuilder: (context, index) => const SizedBox(width: 12),
        itemBuilder: (context, i) => VehicleCardWidget(vehicle: vehicles[i]),
      ),
    );
  }
}

class VehicleCardWidget extends StatelessWidget {
  final VehicleCard vehicle;
  const VehicleCardWidget({super.key, required this.vehicle});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final isWarning = vehicle.status == VehicleStatus.expiringSoon;
    final isError = vehicle.status == VehicleStatus.expired;
    
    final statusColor = isError 
        ? AppTheme.error 
        : (isWarning ? Colors.orange.shade700 : AppTheme.primary);

    final statusText = switch (vehicle.status) {
      VehicleStatus.active => l10n.statusActive,
      VehicleStatus.expiringSoon => l10n.statusExpiringSoon,
      VehicleStatus.expired => l10n.statusExpired,
    };

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
                Expanded(
                  child: Text(
                    vehicle.vehicleNo,
                    style: AppTheme.heading3.copyWith(fontSize: 15),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                const SizedBox(width: 4),
                AppBadge(
                  label: statusText,
                  isFilled: !isError,
                  color: statusColor,
                ),
              ],
            ),
            Text(
              vehicle.packageName ?? l10n.defaultPackage,
              style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
            ),
            Text(
              isError ? l10n.expired : l10n.daysLeft(vehicle.daysLeft),
              style: AppTheme.bodySmall.copyWith(
                color: statusColor,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Active Session ───────────────────────────────────────────────────────
class SessionSection extends StatelessWidget {
  final ActiveSessionCard? session;
  const SessionSection({super.key, required this.session});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    if (session == null) {
      return AppEmptyState(
        icon: Icons.local_parking_outlined,
        title: l10n.noParkingSession,
        subtitle: l10n.notInAnyParkingLot,
      );
    }

    final formatCurrency = NumberFormat.currency(locale: 'vi_VN', symbol: 'đ');
    final timeFormat = DateFormat('HH:mm - dd/MM/yyyy');

    return AppCard(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(session!.vehicleNo, style: AppTheme.heading3),
              if (session!.estimatedFee > 0)
                Text(
                  formatCurrency.format(session!.estimatedFee),
                  style: AppTheme.label.copyWith(color: Colors.orange.shade700),
                ),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Icon(Icons.login, size: 14, color: AppTheme.subtle),
              const SizedBox(width: 4),
              Text(
                l10n.entryTimeLabel(timeFormat.format(session!.entryTime)),
                style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              Icon(Icons.timer_outlined, size: 14, color: AppTheme.subtle),
              const SizedBox(width: 4),
              Text(
                l10n.parkingDurationLabel(session!.formattedDuration),
                style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

// ── Quick Actions ────────────────────────────────────────────────────────
class QuickActions extends StatelessWidget {
  final int pendingPaymentCount;
  const QuickActions({super.key, required this.pendingPaymentCount});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Row(
      children: [
        Expanded(
          child: ActionCard(
            icon: Icons.local_parking_outlined,
            label: l10n.parkingSessionAction,
            onTap: () => context.push(AppRoutes.parkingSessionList),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: ActionCard(
            icon: Icons.card_membership_outlined,
            label: l10n.billingAction,
            badgeCount: pendingPaymentCount,
            onTap: () => context.go(AppRoutes.customerPackages),
          ),
        ),
      ],
    );
  }
}

class ActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final int badgeCount;

  const ActionCard({
    super.key,
    required this.icon,
    required this.label,
    required this.onTap,
    this.badgeCount = 0,
  });

  @override
  Widget build(BuildContext context) {
    return AppCard(
      onTap: onTap,
      padding: const EdgeInsets.all(14),
      child: Stack(
        alignment: Alignment.center,
        clipBehavior: Clip.none,
        children: [
          Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, size: 24, color: AppTheme.primary),
              const SizedBox(height: 8),
              Text(label, style: AppTheme.label, textAlign: TextAlign.center),
            ],
          ),
          if (badgeCount > 0)
            Positioned(
              top: -8,
              right: -8,
              child: Container(
                padding: const EdgeInsets.all(6),
                decoration: const BoxDecoration(
                  color: AppTheme.error,
                  shape: BoxShape.circle,
                ),
                child: Text(
                  badgeCount.toString(),
                  style: AppTheme.caption.copyWith(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 10,
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}
