import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/add_vehicle_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/metadata_models.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

class AddVehicleScreen extends StatefulWidget {
  final int bookingId;
  final int customerId;

  const AddVehicleScreen({
    super.key,
    required this.bookingId,
    required this.customerId,
  });

  @override
  State<AddVehicleScreen> createState() => _AddVehicleScreenState();
}

class _AddVehicleScreenState extends State<AddVehicleScreen> {
  final _plateController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AddVehicleViewModel>().reset();
      context.read<AddVehicleViewModel>().fetchVehicleTypes();
    });
  }

  @override
  void dispose() {
    _plateController.dispose();
    super.dispose();
  }

  void _submit() async {
    if (_plateController.text.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(AppLocalizations.of(context)!.pleaseEnterLicensePlate)));
      return;
    }

    final vm = context.read<AddVehicleViewModel>();

    // We will submit draft directly without payment first.
    // The payment is done on the Draft item later.
    final success = await vm.submitDraft(
      bookingId: widget.bookingId,
      customerId: widget.customerId,
      vehicleNo: _plateController.text,
      startDate: DateTime.now(),
    );

    if (success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(AppLocalizations.of(context)!.addedToCartSuccessfully)),
      );
      context.pop(true); // Return success to trigger refresh
    } else if (mounted) {
      final state = vm.submitState;
      final msg = state is Failure<BookingDetailDto>
          ? state.message
          : AppLocalizations.of(context)!.errorAddingVehicle;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
    }
  }

  @override
  Widget build(BuildContext context) {
    final currencyFormatter = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '₫',
    );

    return Scaffold(
      appBar: AppBar(title: Text(AppLocalizations.of(context)!.addNewVehicleRegistration)),
      body: Consumer<AddVehicleViewModel>(
        builder: (context, vm, child) {
          final typesState = vm.vehicleTypesState;

          if (typesState is Loading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (typesState is Failure) {
            return AppEmptyState(
              icon: Icons.error_outline,
              title: AppLocalizations.of(context)!.dataLoadError,
              subtitle: (typesState as Failure).message,
            );
          }

          if (typesState is Success<List<AllowedVehicleType>>) {
            final vehicleTypes = typesState.data;

            return SingleChildScrollView(
              padding: const EdgeInsets.all(AppTheme.pagePadding),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Vehicle Info Section
                  Text(AppLocalizations.of(context)!.vehicleInformation, style: AppTheme.heading3),
                  const SizedBox(height: 12),
                  AppCard(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      children: [
                        AppTextField(
                          label: AppLocalizations.of(context)!.licensePlate,
                          controller: _plateController,
                          hint: AppLocalizations.of(context)!.licensePlateExample,
                          textCapitalization: TextCapitalization.characters,
                        ),
                        const SizedBox(height: 16),
                        _buildLabel(AppLocalizations.of(context)!.vehicleType),
                        const SizedBox(height: 8),
                        Wrap(
                          spacing: 12,
                          runSpacing: 12,
                          children: vehicleTypes
                              .map((type) => _buildTypeOption(type, vm))
                              .toList(),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppTheme.sectionGap),

                  // Package Selection
                  Text(AppLocalizations.of(context)!.selectPackage, style: AppTheme.heading3),
                  const SizedBox(height: 12),

                  if (vm.packagesState is Loading)
                    const Center(
                      child: Padding(
                        padding: EdgeInsets.all(20),
                        child: CircularProgressIndicator(),
                      ),
                    )
                  else if (vm.packagesState
                      is Success<List<AvailablePackagePrice>>)
                    ...(vm.packagesState
                            as Success<List<AvailablePackagePrice>>)
                        .data
                        .map(
                          (pkg) =>
                              _buildPackageItem(pkg, vm, currencyFormatter),
                        )
                  else if (vm.selectedVehicleType == null)
                    AppCard(
                      padding: const EdgeInsets.all(20),
                      child: Text(AppLocalizations.of(context)!.pleaseSelectVehicleTypeFirst),
                    )
                  else
                    AppCard(
                      padding: const EdgeInsets.all(20),
                      child: Text(AppLocalizations.of(context)!.noSuitablePackageForThisVehicleType),
                    ),

                  const SizedBox(height: 32),

                  // Summary & Submit
                  if (vm.selectedPackage != null) ...[
                    _buildPriceSummary(vm.selectedPackage!, currencyFormatter),
                    const SizedBox(height: 24),
                  ],

                  vm.submitState is Loading
                      ? const Center(child: CircularProgressIndicator())
                      : AppFilledButton(
                          label: AppLocalizations.of(context)!.addToCart,
                          onPressed: _submit,
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

  Widget _buildTypeOption(AllowedVehicleType type, AddVehicleViewModel vm) {
    final isSelected =
        vm.selectedVehicleType?.vehicleTypeId == type.vehicleTypeId;
    final isFull = type.isFull;

    return InkWell(
      onTap: isFull ? null : () => vm.selectVehicleType(type),
      borderRadius: BorderRadius.circular(12),
      child: Container(
        width: 120,
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: isFull
              ? AppTheme.surface.withValues(alpha: 0.5)
              : isSelected
              ? AppTheme.primary.withValues(alpha: 0.1)
              : AppTheme.surface,
          border: Border.all(
            color: isFull
                ? AppTheme.border.withValues(alpha: 0.5)
                : isSelected
                ? AppTheme.primary
                : AppTheme.border,
            width: isSelected ? 2 : 1,
          ),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          children: [
            Icon(
              type.vehicleTypeName.toLowerCase().contains('oto') ||
                      type.vehicleTypeName.toLowerCase().contains('car')
                  ? Icons.directions_car
                  : Icons.motorcycle,
              color: isFull
                  ? AppTheme.subtle.withValues(alpha: 0.5)
                  : isSelected
                  ? AppTheme.primary
                  : AppTheme.subtle,
            ),
            const SizedBox(height: 4),
            Text(
              type.vehicleTypeName,
              textAlign: TextAlign.center,
              style: AppTheme.bodySmall.copyWith(
                color: isFull
                    ? AppTheme.subtle.withValues(alpha: 0.5)
                    : isSelected
                    ? AppTheme.primary
                    : AppTheme.subtle,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              '${type.currentQuantity}/${type.maxQuantity}',
              style: AppTheme.caption.copyWith(
                color: isFull ? AppTheme.error : AppTheme.primary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPackageItem(
    AvailablePackagePrice pkg,
    AddVehicleViewModel vm,
    NumberFormat formatter,
  ) {
    final isSelected = vm.selectedPackage?.packagePriceId == pkg.packagePriceId;
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () => vm.selectPackage(pkg),
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: isSelected
                ? AppTheme.primary.withValues(alpha: 0.05)
                : AppTheme.surface,
            border: Border.all(
              color: isSelected ? AppTheme.primary : AppTheme.border,
              width: isSelected ? 2 : 1,
            ),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Row(
            children: [
              Icon(
                isSelected
                    ? Icons.radio_button_checked
                    : Icons.radio_button_off,
                color: isSelected ? AppTheme.primary : AppTheme.subtle,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      pkg.packagePriceName,
                      style: AppTheme.body.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    Text(
                      '${pkg.durationMonths} ${AppLocalizations.of(context)!.months}',
                      style: AppTheme.bodySmall.copyWith(
                        color: AppTheme.subtle,
                      ),
                    ),
                  ],
                ),
              ),
              Text(
                formatter.format(pkg.price),
                style: AppTheme.body.copyWith(
                  color: AppTheme.primary,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPriceSummary(AvailablePackagePrice pkg, NumberFormat formatter) {
    final startDate = DateTime.now();
    final endDate = DateTime(
      startDate.year,
      startDate.month + pkg.durationMonths,
      startDate.day,
    );
    final dateFormat = DateFormat('dd/MM/yyyy');

    return AppCard(
      padding: const EdgeInsets.all(16),
      color: AppTheme.primary.withValues(alpha: 0.05),
      child: Column(
        children: [
          _buildSummaryRow(AppLocalizations.of(context)!.duration, '${pkg.durationMonths} ${AppLocalizations.of(context)!.months}'),
          const SizedBox(height: 8),
          _buildSummaryRow(AppLocalizations.of(context)!.validFrom, dateFormat.format(startDate)),
          const SizedBox(height: 8),
          _buildSummaryRow(AppLocalizations.of(context)!.validUntil, dateFormat.format(endDate)),
          const Divider(height: 24),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(AppLocalizations.of(context)!.total, style: AppTheme.heading3),
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
        Text(
          value,
          style: AppTheme.bodySmall.copyWith(fontWeight: FontWeight.w600),
        ),
      ],
    );
  }
}
