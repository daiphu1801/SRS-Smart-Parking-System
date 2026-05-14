import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:intl/intl.dart';

class BookingDetailItemScreen extends StatefulWidget {
  final String bookingDetailId;

  const BookingDetailItemScreen({super.key, required this.bookingDetailId});

  @override
  State<BookingDetailItemScreen> createState() =>
      _BookingDetailItemScreenState();
}

class _BookingDetailItemScreenState extends State<BookingDetailItemScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<BookingViewModel>().fetchBookingDetailById(
        widget.bookingDetailId,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Chi tiết Booking Detail')),
      body: Consumer<BookingViewModel>(
        builder: (context, vm, child) {
          return switch (vm.currentBookingDetailState) {
            Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: 'Lỗi tải dữ liệu',
                subtitle: msg,
              ),
            Success(data: var detail) => Builder(builder: (context) {
                final currencyFormatter = NumberFormat.currency(
                  locale: 'vi_VN',
                  symbol: '₫',
                );
                final dateFormatter = DateFormat('dd/MM/yyyy');
                final statusLabel = detail.status.label;

                return SingleChildScrollView(
                  padding: const EdgeInsets.all(AppTheme.pagePadding),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      AppCard(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        'Mã số: ${detail.id}',
                                        style: AppTheme.heading2,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                      const SizedBox(height: 4),
                                      Text(
                                        detail.plateNumber,
                                        style: AppTheme.body.copyWith(
                                          color: AppTheme.subtle,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                AppBadge(
                                  label: statusLabel,
                                  isFilled: detail.isActive,
                                ),
                              ],
                            ),
                            const Divider(height: 32),
                            _buildDetailRow('Booking Detail ID:', detail.id),
                            const SizedBox(height: 12),
                            _buildDetailRow('Booking ID:', detail.bookingId),
                            const SizedBox(height: 12),
                            _buildDetailRow('Customer ID:', detail.customerId),
                            const SizedBox(height: 12),
                            _buildDetailRow(
                              'Package Price ID:',
                              detail.packagePriceId ?? '-',
                            ),
                            const SizedBox(height: 12),
                            _buildDetailRow('Biển số xe:', detail.plateNumber),
                            const SizedBox(height: 12),
                            _buildDetailRow('Loại xe:', detail.vehicleType),
                            const SizedBox(height: 12),
                            _buildDetailRow('Gói cước:', detail.packageType),
                            const SizedBox(height: 12),
                            _buildDetailRow('Thời hạn:', detail.duration),
                            const SizedBox(height: 12),
                            _buildDetailRow('Trạng thái:', statusLabel),
                            const SizedBox(height: 12),
                            _buildDetailRow(
                              'Giá cước:',
                              currencyFormatter.format(detail.price),
                              highlight: true,
                            ),
                            const SizedBox(height: 12),
                            _buildDetailRow(
                              'Ngày bắt đầu:',
                              dateFormatter.format(detail.startDate),
                            ),
                            const SizedBox(height: 12),
                            _buildDetailRow(
                              'Ngày kết thúc:',
                              dateFormatter.format(detail.endDate),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: AppTheme.sectionGap),
                      AppFilledButton(
                        label: 'Gia hạn hợp đồng',
                        onPressed: () {
                          context.push(
                            '/customer/booking-detail/renew/${detail.id}',
                          );
                        },
                      ),
                    ],
                  ),
                );
              }),
            _ => const SizedBox.shrink(),
          };
        },
      ),
    );
  }

  Widget _buildDetailRow(String label, String value, {bool highlight = false}) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: AppTheme.body.copyWith(color: AppTheme.subtle)),
        Text(
          value,
          style: highlight
              ? AppTheme.heading3.copyWith(color: AppTheme.primary)
              : AppTheme.body.copyWith(fontWeight: FontWeight.w600),
        ),
      ],
    );
  }
}
