import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';

/// Card showing individual booking detail (vehicle package)
class BookingDetailCard extends StatelessWidget {
  final BookingDetail detail;
  const BookingDetailCard({super.key, required this.detail});

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy');
    final currencyFormatter = NumberFormat.currency(locale: 'vi_VN', symbol: '₫');

    final daysRemaining = detail.remainingDays;
    final isExpiringSoon = detail.isActive && daysRemaining <= 7;

    return AppCard(
      onTap: () => context.push('/customer/booking-detail/${detail.id}'),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Row(
                  children: [
                    Icon(
                      detail.vehicleType == 'CAR'
                          ? Icons.directions_car_outlined
                          : Icons.two_wheeler_outlined,
                      size: 16,
                      color: AppTheme.primary,
                    ),
                    const SizedBox(width: 6),
                    Flexible(
                      child: Text(
                        detail.plateNumber,
                        style: AppTheme.heading3.copyWith(fontSize: 16),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              AppBadge(
                label: detail.status.label,
                isFilled: detail.isActive,
              ),
            ],
          ),
          const SizedBox(height: 8),
          _buildTinyLabel('Gói: ${detail.packageType} · ${detail.duration}'),
          const SizedBox(height: 4),
          _buildTinyLabel('Hiệu lực: ${dateFormatter.format(detail.startDate)} → ${dateFormatter.format(detail.endDate)}'),
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
                  Icon(Icons.warning_amber_outlined, size: 13, color: Colors.orange.shade700),
                  const SizedBox(width: 4),
                  Text(
                    'Còn $daysRemaining ngày — Gia hạn ngay!',
                    style: AppTheme.caption.copyWith(color: Colors.orange.shade700),
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
              Text('Hợp đồng ${booking.id}', style: AppTheme.heading1.copyWith(fontSize: 24)),
              const SizedBox(height: 8),
              AppBadge(
                label: booking.paymentStatus.label, 
                isFilled: booking.paymentStatus == PaymentStatus.success,
              ),
            ],
          ),
        ),
        const SizedBox(height: 36),
        Text('Thông tin tổng quan', style: AppTheme.heading3),
        const SizedBox(height: 16),
        AppCard(
          padding: const EdgeInsets.all(20),
          child: Column(
            children: [
              _buildInfoRow('Nhóm / đại diện', booking.groupName),
              const Divider(height: 32),
              _buildInfoRow('Ngày tạo', dateFormatter.format(booking.createdAt)),
              const Divider(height: 32),
              _buildInfoRow('Số xe đăng ký', '${booking.totalVehicles} xe'),
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
        Flexible(child: Text(label, style: AppTheme.body.copyWith(color: AppTheme.subtle))),
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
