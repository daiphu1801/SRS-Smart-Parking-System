import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:intl/intl.dart';

class AddVehicleScreen extends StatefulWidget {
  final String bookingId;
  const AddVehicleScreen({
    super.key,
    required this.bookingId,
  });

  @override
  State<AddVehicleScreen> createState() => _AddVehicleScreenState();
}

class _AddVehicleScreenState extends State<AddVehicleScreen> {
  final _plateController = TextEditingController();
  String _selectedVehicleType = 'CAR';
  String? _selectedPackagePriceId;
  bool _isSubmitting = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final bookingVM = context.read<BookingViewModel>();
      bookingVM.fetchBookingById(widget.bookingId);
      bookingVM.fetchPackagePrices();
    });
  }

  @override
  void dispose() {
    _plateController.dispose();
    super.dispose();
  }

  void _submit() async {
    if (_plateController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng nhập biển số xe')),
      );
      return;
    }

    if (_selectedPackagePriceId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng chọn gói cước')),
      );
      return;
    }

    final bookingVM = context.read<BookingViewModel>();
    final packageState = bookingVM.packagePricesState;
    if (packageState is! Success<List<PackagePrice>>) return;

    final selectedPkg = packageState.data.firstWhere((p) => p.id == _selectedPackagePriceId);

    // Navigate to payment first
    final result = await context.push<bool>(
      '/payment/qr',
      extra: {
        'amount': selectedPkg.price,
        'targetId': widget.bookingId,
        'isSession': false,
      },
    );

    if (result == true && mounted) {
      setState(() => _isSubmitting = true);
      
      final detail = await bookingVM.addVehicleToBookingForCustomer(
        bookingId: widget.bookingId,
        customerId: bookingVM.currentBookingState is Success<Booking> 
            ? (bookingVM.currentBookingState as Success<Booking>).data.groupId 
            : '',
        plateNumber: _plateController.text,
        vehicleType: _selectedVehicleType,
        packagePriceId: _selectedPackagePriceId!,
      );

      setState(() => _isSubmitting = false);

      if (detail != null && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Thêm xe vào hợp đồng thành công!')),
        );
        context.pop(); // Return to booking detail
      } else if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Có lỗi xảy ra khi thêm xe')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final currencyFormatter = NumberFormat.currency(locale: 'vi_VN', symbol: '₫');

    return Scaffold(
      appBar: AppBar(
        title: const Text('Thêm xe vào hợp đồng'),
      ),
      body: Consumer<BookingViewModel>(
        builder: (context, bookingVM, child) {
          final bookingState = bookingVM.currentBookingState;
          
          if (bookingState is Loading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (bookingState is Failure) {
            return AppEmptyState(
              icon: Icons.error_outline,
              title: 'Lỗi tải dữ liệu',
              subtitle: bookingState is Failure ? (bookingState as Failure).message : null,
            );
          }

          if (bookingState is Success<Booking>) {
            final booking = bookingState.data;
            final packageState = bookingVM.packagePricesState;
            // Filter packages by vehicle type
            List<PackagePrice> filteredPackages = [];
            if (packageState is Success<List<PackagePrice>>) {
              filteredPackages = packageState.data
                  .where((p) => p.vehicleType == _selectedVehicleType)
                  .toList();
            }

            // Auto select first package if none selected or type changed
            if (_selectedPackagePriceId == null && filteredPackages.isNotEmpty) {
              _selectedPackagePriceId = filteredPackages.first.id;
            } else if (_selectedPackagePriceId != null && 
                       !filteredPackages.any((p) => p.id == _selectedPackagePriceId)) {
               _selectedPackagePriceId = filteredPackages.isNotEmpty ? filteredPackages.first.id : null;
            }

            return SingleChildScrollView(
              padding: const EdgeInsets.all(AppTheme.pagePadding),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Contract Info Header
                  AppCard(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text('Hợp đồng: ${booking.id}', style: AppTheme.heading3),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                color: AppTheme.primary.withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Text(
                                '${booking.details.length}/${booking.totalVehicles} xe',
                                style: AppTheme.bodySmall.copyWith(color: AppTheme.primary, fontWeight: FontWeight.bold),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(booking.groupName, style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppTheme.sectionGap),

                  // Vehicle Info Section
                  Text('Thông tin phương tiện', style: AppTheme.heading3),
                  const SizedBox(height: 12),
                  AppCard(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      children: [
                        AppTextField(
                          label: 'Biển số xe',
                          controller: _plateController,
                          hint: 'Ví dụ: 30A-123.45',
                          textCapitalization: TextCapitalization.characters,
                        ),
                        const SizedBox(height: 16),
                        _buildLabel('Loại xe'),
                        const SizedBox(height: 8),
                        Row(
                          children: [
                            _buildTypeOption('CAR', Icons.directions_car, 'Ô tô'),
                            const SizedBox(width: 12),
                            _buildTypeOption('BIKE', Icons.motorcycle, 'Xe máy'),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppTheme.sectionGap),

                  // Package Selection
                  Text('Chọn gói cước', style: AppTheme.heading3),
                  const SizedBox(height: 12),
                  if (packageState is Loading)
                    const Center(child: Padding(padding: EdgeInsets.all(20), child: CircularProgressIndicator()))
                  else if (filteredPackages.isEmpty)
                    const AppCard(
                      padding: EdgeInsets.all(20),
                      child: Text('Không có gói cước phù hợp cho loại xe này'),
                    )
                  else
                    ...filteredPackages.map((pkg) => _buildPackageItem(pkg, currencyFormatter)),

                  const SizedBox(height: 32),

                  // Summary & Submit
                  if (_selectedPackagePriceId != null) ...[
                     _buildPriceSummary(filteredPackages.firstWhere((p) => p.id == _selectedPackagePriceId), currencyFormatter),
                     const SizedBox(height: 24),
                  ],

                  _isSubmitting
                      ? const Center(child: CircularProgressIndicator())
                      : AppFilledButton(
                          label: 'Thanh toán & Thêm xe',
                          onPressed: _submit,
                          // Disable if reached limit
                          // disabled: booking.details.length >= booking.totalVehicles,
                        ),
                  if (booking.details.length >= booking.totalVehicles)
                     Padding(
                       padding: const EdgeInsets.only(top: 8),
                       child: Text(
                         'Hợp đồng đã đạt giới hạn tối đa (${booking.totalVehicles} xe)',
                         textAlign: TextAlign.center,
                         style: AppTheme.bodySmall.copyWith(color: AppTheme.error),
                       ),
                     ),
                  const SizedBox(height: 40),
                ],
              ),
            );
          }

          return const SizedBox.shrink();
        },
      ),
    );
  }

  Widget _buildLabel(String label) {
    return Align(
      alignment: Alignment.centerLeft,
      child: Text(
        label,
        style: AppTheme.bodySmall.copyWith(
          color: AppTheme.subtle,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }

  Widget _buildTypeOption(String type, IconData icon, String label) {
    final isSelected = _selectedVehicleType == type;
    return Expanded(
      child: InkWell(
        onTap: () => setState(() => _selectedVehicleType = type),
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            color: isSelected ? AppTheme.primary.withValues(alpha: 0.1) : AppTheme.surface,
            border: Border.all(
              color: isSelected ? AppTheme.primary : AppTheme.border,
              width: isSelected ? 2 : 1,
            ),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            children: [
              Icon(icon, color: isSelected ? AppTheme.primary : AppTheme.subtle),
              const SizedBox(height: 4),
              Text(
                label,
                style: AppTheme.bodySmall.copyWith(
                  color: isSelected ? AppTheme.primary : AppTheme.subtle,
                  fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPackageItem(PackagePrice pkg, NumberFormat formatter) {
    final isSelected = _selectedPackagePriceId == pkg.id;
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () => setState(() => _selectedPackagePriceId = pkg.id),
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: isSelected ? AppTheme.primary.withValues(alpha: 0.05) : AppTheme.surface,
            border: Border.all(
              color: isSelected ? AppTheme.primary : AppTheme.border,
              width: isSelected ? 2 : 1,
            ),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Row(
            children: [
              Radio<String>(
                value: pkg.id,
                groupValue: _selectedPackagePriceId,
                onChanged: (val) => setState(() => _selectedPackagePriceId = val),
                activeColor: AppTheme.primary,
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(pkg.packageName, style: AppTheme.body.copyWith(fontWeight: FontWeight.bold)),
                    Text('${pkg.durationDays} ngày', style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                  ],
                ),
              ),
              Text(
                formatter.format(pkg.price),
                style: AppTheme.body.copyWith(color: AppTheme.primary, fontWeight: FontWeight.bold),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPriceSummary(PackagePrice pkg, NumberFormat formatter) {
    final startDate = DateTime.now();
    final endDate = startDate.add(Duration(days: pkg.durationDays));
    final dateFormat = DateFormat('dd/MM/yyyy');

    return AppCard(
      padding: const EdgeInsets.all(16),
      color: AppTheme.primary.withValues(alpha: 0.05),
      child: Column(
        children: [
          _buildSummaryRow('Thời hạn', '${pkg.durationDays} ngày'),
          const SizedBox(height: 8),
          _buildSummaryRow('Hiệu lực từ', dateFormat.format(startDate)),
          const SizedBox(height: 8),
          _buildSummaryRow('Đến ngày', dateFormat.format(endDate)),
          const Divider(height: 24),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Tổng cộng', style: AppTheme.heading3),
              Text(
                formatter.format(pkg.price),
                style: AppTheme.heading2.copyWith(color: AppTheme.primary),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryRow(String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
        Text(value, style: AppTheme.bodySmall.copyWith(fontWeight: FontWeight.w600)),
      ],
    );
  }
}
