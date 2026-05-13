import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:intl/intl.dart';

class RenewPackageScreen extends StatefulWidget {
  final String bookingDetailId;

  const RenewPackageScreen({super.key, required this.bookingDetailId});

  @override
  State<RenewPackageScreen> createState() => _RenewPackageScreenState();
}

class _RenewPackageScreenState extends State<RenewPackageScreen> {
  int _selectedMonths = 1;
  bool _isSubmitting = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<BookingViewModel>().fetchBookingDetailById(widget.bookingDetailId);
    });
  }

  void _submitRenewal(BookingDetail oldDetail, double totalPrice) async {
    // Chuyển sang màn hình thanh toán QR
    final result = await context.push<bool>(
      '/payment/qr',
      extra: {
        'amount': totalPrice,
        'targetId': oldDetail.id,
        'isSession': false,
      },
    );

    // Nếu thanh toán thành công (webhook mock trả về true)
    if (result == true && mounted) {
      setState(() => _isSubmitting = true);
      
      // Sau khi thanh toán thành công, gọi API để gia hạn gói cước
      final vm = context.read<BookingViewModel>();
      final success = await vm.renewBookingDetail(oldDetail, _selectedMonths);
      
      setState(() => _isSubmitting = false);

      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Gia hạn thành công!')),
        );
        context.pop(); // Go back to detail
        context.pop(); // Go back to list/home to see the updated data
      } else if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Gia hạn thất bại ở bước cập nhật server.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Gia hạn Booking Detail'),
      ),
      body: Consumer<BookingViewModel>(
        builder: (context, vm, child) {
          final state = vm.currentBookingDetailState;

          if (state is Loading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is Failure) {
            return AppEmptyState(
              icon: Icons.error_outline,
              title: 'Lỗi tải dữ liệu',
              subtitle: (state as Failure).message,
            );
          }

          if (state is Success<BookingDetail>) {
            final detail = state.data;
            final currencyFormatter = NumberFormat.currency(locale: 'vi_VN', symbol: '₫');
            final totalPrice = detail.price * _selectedMonths;

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
                        Text('Thông tin xe', style: AppTheme.heading3),
                        const SizedBox(height: 12),
                        AppTextField(
                          label: 'Booking Detail ID',
                          controller: TextEditingController(text: detail.id),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: 'Booking ID',
                          controller: TextEditingController(text: detail.bookingId),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: 'Biển số xe',
                          controller: TextEditingController(text: detail.plateNumber),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: 'Loại xe',
                          controller: TextEditingController(text: detail.vehicleType),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: 'Loại gói',
                          controller: TextEditingController(text: detail.packageType),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: 'Package Price ID',
                          controller: TextEditingController(text: detail.packagePriceId ?? '-'),
                          readOnly: true,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppTheme.sectionGap),
                  
                  AppCard(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Gia hạn', style: AppTheme.heading3),
                        const SizedBox(height: 12),
                        Text('Chọn số tháng gia hạn:', style: AppTheme.body),
                        const SizedBox(height: 12),
                        Row(
                          children: [
                            _buildMonthOption(1),
                            const SizedBox(width: 8),
                            _buildMonthOption(3),
                            const SizedBox(width: 8),
                            _buildMonthOption(6),
                            const SizedBox(width: 8),
                            _buildMonthOption(12),
                          ],
                        ),
                        const Divider(height: 32),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text('Tổng tiền:', style: AppTheme.body),
                            Text(
                              currencyFormatter.format(totalPrice),
                              style: AppTheme.heading2.copyWith(color: AppTheme.primary),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 32),
                  _isSubmitting
                      ? const Center(child: CircularProgressIndicator())
                      : AppFilledButton(
                          label: 'Thanh toán & Gia hạn',
                          onPressed: () => _submitRenewal(detail, totalPrice),
                        ),
                ],
              ),
            );
          }

          return const SizedBox.shrink();
        },
      ),
    );
  }

  Widget _buildMonthOption(int months) {
    final isSelected = _selectedMonths == months;
    return Expanded(
      child: GestureDetector(
        onTap: () => setState(() => _selectedMonths = months),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            color: isSelected ? AppTheme.primary.withOpacity(0.1) : AppTheme.surface,
            border: Border.all(
              color: isSelected ? AppTheme.primary : AppTheme.border,
              width: isSelected ? 2 : 1,
            ),
            borderRadius: BorderRadius.circular(12),
          ),
          alignment: Alignment.center,
          child: Text(
            '$months tháng',
            style: AppTheme.body.copyWith(
              color: isSelected ? AppTheme.primary : AppTheme.primary,
              fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
            ),
          ),
        ),
      ),
    );
  }
}
