import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/utils/enum_localizations.dart';

/// Card showing individual booking detail (vehicle package).
///
/// Supports an optional multi-select mode for DRAFT items:
/// • Pass [isSelected] and [onSelectedChanged] to enable checkbox selection.
/// • In selection mode, tapping the card toggles the checkbox; a small
///   chevron at the trailing edge still navigates to the detail screen.
/// • When selection is not enabled, the original tap-to-navigate behaviour
///   is preserved.
class BookingDetailCard extends StatelessWidget {
  final BookingDetail detail;

  /// Whether this card is currently selected (multi-select mode).
  /// Pass `null` to disable multi-select UI.
  final bool? isSelected;

  /// Called when the user toggles this card's selection state.
  /// If `null`, multi-select mode is disabled.
  final ValueChanged<bool?>? onSelectedChanged;

  /// Whether to hide action buttons (pay, renew) in the detail screen.
  final bool hideActions;

  const BookingDetailCard({
    super.key,
    required this.detail,
    this.isSelected,
    this.onSelectedChanged,
    this.hideActions = false,
  });

  bool get _isSelectionMode =>
      onSelectedChanged != null && detail.isDraft;

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy');
    final currencyFormatter = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '₫',
    );

    final daysRemaining = detail.remainingDays;
    final isExpiringSoon = detail.isActive && daysRemaining <= 7;

    return AppCard(
      onTap: _isSelectionMode
          ? () => onSelectedChanged!(!(isSelected ?? false))
          : () => context.push(
                '/customer/booking-detail/${detail.id}',
                extra: {'hideActions': hideActions},
              ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              // ── Leading checkbox (only for DRAFT in selection mode) ──
              if (_isSelectionMode) ...[
                Checkbox(
                  value: isSelected ?? false,
                  onChanged: onSelectedChanged,
                  activeColor: AppTheme.primary,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
                const SizedBox(width: 10),
              ],

              // ── Vehicle icon + plate ──
              Expanded(
                child: Row(
                  children: [
                      Icon(
                        detail.vehicleType.toUpperCase().contains('CAR') || detail.vehicleType.toUpperCase().contains('Ô TÔ')
                            ? Icons.directions_car_outlined
                            : Icons.two_wheeler_outlined,
                        size: 16,
                        color: AppTheme.primary,
                      ),
                      const SizedBox(width: 6),
                      Flexible(
                        child: Text(
                          '${detail.plateNumber} - ${detail.vehicleType}',
                          style: AppTheme.heading3.copyWith(fontSize: 16),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              AppBadge(label: detail.status.label(context), isFilled: detail.isActive),

              // ── Trailing chevron to detail (selection mode only) ──
              if (_isSelectionMode) ...[
                const SizedBox(width: 4),
                GestureDetector(
                  behavior: HitTestBehavior.opaque,
                  onTap: () => context.push(
                    '/customer/booking-detail/${detail.id}',
                    extra: {'hideActions': hideActions},
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(4.0),
                    child: Icon(
                      Icons.chevron_right,
                      size: 20,
                      color: AppTheme.subtle,
                    ),
                  ),
                ),
              ],
            ],
          ),
          const SizedBox(height: 8),
          _buildTinyLabel('${AppLocalizations.of(context)!.packageLabel} ${detail.packageType} · ${detail.duration}'),
          const SizedBox(height: 4),
          _buildTinyLabel(
            '${AppLocalizations.of(context)!.validityPeriod} ${dateFormatter.format(detail.startDate)} → ${dateFormatter.format(detail.endDate)}',
          ),
          if (isExpiringSoon) ...[
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 5),
              decoration: BoxDecoration(
                color: Colors.orange.shade50,
                borderRadius: BorderRadius.circular(6),
                border: Border.all(color: Colors.orange.shade200),
              ),
              child: Row(
                children: [
                  Icon(
                    Icons.warning_amber_outlined,
                    size: 13,
                    color: Colors.orange.shade700,
                  ),
                  const SizedBox(width: 4),
                  Text(
                    AppLocalizations.of(context)!.daysRemainingRenewNow(daysRemaining),
                    style: AppTheme.caption.copyWith(
                      color: Colors.orange.shade700,
                    ),
                  ),
                ],
              ),
            ),
          ],
          const SizedBox(height: 8),
          Align(
            alignment: Alignment.centerRight,
            child: Text(
              currencyFormatter.format(detail.price),
              style: AppTheme.body.copyWith(
                fontWeight: FontWeight.w700,
                color: AppTheme.primary,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTinyLabel(String text) {
    return Text(
      text,
      style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
    );
  }
}

/// Overview header for a Booking (Contract)
class BookingOverviewHeader extends StatelessWidget {
  final Booking booking;

  const BookingOverviewHeader({super.key, required this.booking});

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const SizedBox(height: 16),
        Center(
          child: Column(
            children: [
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: AppTheme.primary.withValues(alpha: 0.1),
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.receipt_long_rounded,
                  size: 48,
                  color: AppTheme.primary,
                ),
              ),
              const SizedBox(height: 24),
              Text(
                AppLocalizations.of(context)!.contractWithId(booking.id),
                style: AppTheme.heading1.copyWith(fontSize: 24),
              ),
              const SizedBox(height: 8),
              AppBadge(
                label: booking.paymentStatus.label(context),
                isFilled: booking.paymentStatus == PaymentStatus.success,
              ),
            ],
          ),
        ),
        const SizedBox(height: 36),
        Text(AppLocalizations.of(context)!.overviewInfo, style: AppTheme.heading3),
        const SizedBox(height: 16),
        AppCard(
          padding: const EdgeInsets.all(20),
          child: Column(
            children: [
              _buildInfoRow(AppLocalizations.of(context)!.groupRepresentativeLabel, booking.groupName),
              const Divider(height: 32),
              _buildInfoRow(
                AppLocalizations.of(context)!.createdAtLabel,
                dateFormatter.format(booking.createdAt),
              ),
              const Divider(height: 32),
              _buildInfoRow(AppLocalizations.of(context)!.registeredVehiclesCountLabel, AppLocalizations.of(context)!.vehiclesCountText(booking.totalVehicles)),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildInfoRow(String label, String value, {bool isTotal = false}) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Flexible(
          child: Text(
            label,
            style: AppTheme.body.copyWith(color: AppTheme.subtle),
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Text(
            value,
            textAlign: TextAlign.right,
            style: isTotal
                ? AppTheme.heading2.copyWith(color: AppTheme.primary)
                : AppTheme.heading3,
          ),
        ),
      ],
    );
  }
}

/// Banner alerting user that they have pending payments and guiding them to History tab.
class PendingPaymentAlertBanner extends StatelessWidget {
  final int count;

  const PendingPaymentAlertBanner({
    super.key,
    required this.count,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Colors.orange.shade50.withValues(alpha: 0.9),
            Colors.orange.shade100.withValues(alpha: 0.6),
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
        boxShadow: [
          BoxShadow(
            color: Colors.orange.shade100.withValues(alpha: 0.2),
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Elegant trailing payment icon with rotating amber pulse
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Colors.orange.shade200.withValues(alpha: 0.4),
              shape: BoxShape.circle,
            ),
            child: Icon(
              Icons.hourglass_empty_rounded,
              color: Colors.orange.shade800,
              size: 20,
            ),
          ),
          const SizedBox(width: 12),
          // Banner text details
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  AppLocalizations.of(context)!.pendingPaymentsBannerTitle(count),
                  style: AppTheme.heading3.copyWith(
                    color: Colors.orange.shade900,
                    fontSize: 15,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  AppLocalizations.of(context)!.pendingPaymentsBannerSubtitle,
                  style: AppTheme.bodySmall.copyWith(
                    color: Colors.orange.shade800,
                    height: 1.35,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          // Action button to go to history
          IconButton(
            onPressed: () => context.go('/customer/history'),
            style: IconButton.styleFrom(
              backgroundColor: Colors.orange.shade700,
              foregroundColor: Colors.white,
              padding: const EdgeInsets.all(8),
              minimumSize: Size.zero,
              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            icon: const Icon(
              Icons.arrow_forward_rounded,
              size: 16,
            ),
            tooltip: AppLocalizations.of(context)!.goToHistoryTooltip,
          ),
        ],
      ),
    );
  }
}
