import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/metadata_models.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/features/customer_packages/repositories/booking_repository.dart';
import 'package:smart_parking_mobile/core/di/service_locator.dart';

class RenewPackageScreen extends StatefulWidget {
  final String bookingDetailId;

  const RenewPackageScreen({super.key, required this.bookingDetailId});

  @override
  State<RenewPackageScreen> createState() => _RenewPackageScreenState();
}

class _RenewPackageScreenState extends State<RenewPackageScreen> {
  List<AvailablePackagePrice>? _availablePackages;
  AvailablePackagePrice? _selectedPackage;
  bool _isSubmitting = false;
  bool _isLoadingPackages = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      await context.read<BookingViewModel>().fetchBookingDetailById(
        widget.bookingDetailId,
      );

      if (mounted) {
        final state = context.read<BookingViewModel>().currentBookingDetailState;
        if (state is Success<BookingDetail>) {
          final detail = state.data;
          if (detail.vehicleTypeId != null) {
            setState(() => _isLoadingPackages = true);
            try {
              final repo = sl<BookingRepository>();
              final pkgs = await repo.getAvailablePackages(detail.vehicleTypeId!);
              if (mounted) {
                setState(() {
                  _availablePackages = pkgs;
                  if (pkgs.isNotEmpty) _selectedPackage = pkgs.first;
                });
              }
            } catch (e) {
              // Ignore or show error
            } finally {
              if (mounted) setState(() => _isLoadingPackages = false);
            }
          }
        }
      }
    });
  }

  void _submitRenewal(BookingDetail oldDetail, double totalPrice) async {
    setState(() => _isSubmitting = true);

    // Gọi API để gia hạn gói cước (Thêm vào giỏ hàng)
    final vm = context.read<BookingViewModel>();
    final success = await vm.renewBookingDetail(oldDetail, _selectedPackage!.packagePriceId);

    setState(() => _isSubmitting = false);

    if (success && mounted) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Thêm vào giỏ hàng thành công!')));
      context.pop(); // Go back to detail
      context.pop(); // Go back to list/home
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Không thể thêm vào giỏ hàng. Vui lòng thử lại!'),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(AppLocalizations.of(context)!.renewBookingDetail)),
      body: Consumer<BookingViewModel>(
        builder: (context, vm, child) {
          final state = vm.currentBookingDetailState;

          if (state is Loading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is Failure) {
            return AppEmptyState(
              icon: Icons.error_outline,
              title: AppLocalizations.of(context)!.dataLoadError,
              subtitle: (state as Failure).message,
            );
          }

          if (state is Success<BookingDetail>) {
            final detail = state.data;
            final currencyFormatter = NumberFormat.currency(
              locale: 'vi_VN',
              symbol: '₫',
            );
            final totalPrice = _selectedPackage?.price ?? 0.0;

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
                        Text(AppLocalizations.of(context)!.vehicleInformation, style: AppTheme.heading3),
                        const SizedBox(height: 12),
                        AppTextField(
                          label: AppLocalizations.of(context)!.bookingDetailId,
                          controller: TextEditingController(text: detail.id),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: AppLocalizations.of(context)!.bookingId,
                          controller: TextEditingController(
                            text: detail.bookingId,
                          ),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: AppLocalizations.of(context)!.licensePlate,
                          controller: TextEditingController(
                            text: detail.plateNumber,
                          ),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: AppLocalizations.of(context)!.vehicleType,
                          controller: TextEditingController(
                            text: detail.vehicleType,
                          ),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: AppLocalizations.of(context)!.packageType,
                          controller: TextEditingController(
                            text: detail.packageType,
                          ),
                          readOnly: true,
                        ),
                        const SizedBox(height: 16),
                        AppTextField(
                          label: AppLocalizations.of(context)!.packagePriceId,
                          controller: TextEditingController(
                            text: detail.packagePriceId ?? '-',
                          ),
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
                        Text(AppLocalizations.of(context)!.renew, style: AppTheme.heading3),
                        const SizedBox(height: 12),
                        const SizedBox(height: 12),
                        if (_isLoadingPackages)
                          const Center(child: CircularProgressIndicator())
                        else if (_availablePackages == null || _availablePackages!.isEmpty)
                          Text('Không có gói cước nào khả dụng.', style: AppTheme.body.copyWith(color: AppTheme.error))
                        else
                          Column(
                            children: _availablePackages!.map((pkg) => _buildPackageOption(pkg)).toList(),
                          ),
                        const Divider(height: 32),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(AppLocalizations.of(context)!.totalAmount, style: AppTheme.body),
                            Text(
                              currencyFormatter.format(totalPrice),
                              style: AppTheme.heading2.copyWith(
                                color: AppTheme.primary,
                              ),
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
                          label: 'Thêm vào giỏ hàng',
                          onPressed: _selectedPackage == null
                              ? null
                              : () => _submitRenewal(detail, totalPrice),
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

  Widget _buildPackageOption(AvailablePackagePrice pkg) {
    final isSelected = _selectedPackage?.packagePriceId == pkg.packagePriceId;
    final currencyFormatter = NumberFormat.currency(locale: 'vi_VN', symbol: '₫');

    return GestureDetector(
      onTap: () => setState(() => _selectedPackage = pkg),
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isSelected
              ? AppTheme.primary.withValues(alpha: 0.1)
              : AppTheme.surface,
          border: Border.all(
            color: isSelected ? AppTheme.primary : AppTheme.border,
            width: isSelected ? 2 : 1,
          ),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  pkg.packagePriceName,
                  style: AppTheme.body.copyWith(
                    fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                    color: isSelected ? AppTheme.primary : null,
                  ),
                ),
                Text(
                  '${pkg.durationMonths} tháng',
                  style: AppTheme.caption,
                ),
              ],
            ),
            Text(
              currencyFormatter.format(pkg.price),
              style: AppTheme.body.copyWith(
                fontWeight: FontWeight.w600,
                color: isSelected ? AppTheme.primary : null,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
