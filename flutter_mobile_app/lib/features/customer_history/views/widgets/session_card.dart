import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

class SessionCard extends StatelessWidget {
  final ParkingSession session;
  const SessionCard({super.key, required this.session});

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');
    final currencyFormatter = NumberFormat.currency(locale: 'vi_VN', symbol: 'đ');

    return AppCard(
      onTap: () => context.push(
        AppRoutes.parkingSessionDetail.replaceAll(':id', session.id),
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(
                    session.vehicleType == 'CAR'
                        ? Icons.directions_car
                        : Icons.two_wheeler,
                    color: AppTheme.primary,
                    size: 20,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    session.plateNumber,
                    style: AppTheme.heading3.copyWith(fontSize: 16),
                  ),
                ],
              ),
              AppBadge(
                label: session.isPaid
                    ? AppLocalizations.of(context)!.paidStatus
                    : AppLocalizations.of(context)!.unpaidStatus,
                isFilled: true,
                color: session.isPaid ? Colors.green : Colors.orange.shade700,
              ),
            ],
          ),
          const Divider(height: 24),
          Row(
            children: [
              Icon(Icons.login, size: 16, color: AppTheme.subtle),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  '${AppLocalizations.of(context)!.entryLabel} ${dateFormatter.format(session.entryTime)}',
                  style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              Icon(Icons.logout, size: 16, color: AppTheme.subtle),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  session.exitTime != null
                      ? '${AppLocalizations.of(context)!.exitLabel} ${dateFormatter.format(session.exitTime!)}'
                      : AppLocalizations.of(context)!.notExitedYet,
                  style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                session.durationFormatted,
                style: AppTheme.body.copyWith(fontWeight: FontWeight.w600),
              ),
              Text(
                currencyFormatter.format(session.amountDue),
                style: AppTheme.heading3.copyWith(
                  color: AppTheme.primary,
                  fontSize: 16,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
