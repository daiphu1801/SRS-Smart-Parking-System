import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/utils/enum_localizations.dart';

class BookingDetailItemScreen extends StatefulWidget {
  final String bookingDetailId;
  final bool hideActions;

  const BookingDetailItemScreen({
    super.key,
    required this.bookingDetailId,
    this.hideActions = false,
  });

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
      appBar: AppBar(title: Text(AppLocalizations.of(context)!.bookingDetailDetails)),
      body: Consumer<BookingViewModel>(
        builder: (context, vm, child) {
          return switch (vm.currentBookingDetailState) {
            Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
              icon: Icons.error_outline,
              title: AppLocalizations.of(context)!.dataLoadError,
              subtitle: msg,
            ),
            Success(data: var detail) => Builder(
              builder: (context) {
                final currencyFormatter = NumberFormat.currency(
                  locale: 'vi_VN',
                  symbol: '₫',
                );
                final dateFormatter = DateFormat('dd/MM/yyyy');
                final statusLabel = detail.status.label(context);

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
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        detail.plateNumber,
                                        style: AppTheme.heading2,
                                        overflow: TextOverflow.ellipsis,
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
                            _buildDetailRow(
                              'Tên khách hàng',
                              detail.customerName,
                            ),
                            const SizedBox(height: 12),
                            _buildDetailRow(
                              'Số điện thoại',
                              detail.customerPhone,
                            ),
                            const SizedBox(height: 12),
                            _buildDetailRow(AppLocalizations.of(context)!.licensePlateLabel, detail.plateNumber),
                            const SizedBox(height: 12),
                            _buildDetailRow(AppLocalizations.of(context)!.vehicleTypeLabel, detail.vehicleType),
                            const SizedBox(height: 12),
                            _buildDetailRow(AppLocalizations.of(context)!.packageTypeLabel, detail.packageType),
                            const SizedBox(height: 12),
                            _buildDetailRow(AppLocalizations.of(context)!.durationLabel, detail.duration),
                            const SizedBox(height: 12),
                            _buildDetailRow(AppLocalizations.of(context)!.statusLabel, statusLabel),
                            const SizedBox(height: 12),
                            _buildDetailRow(
                              AppLocalizations.of(context)!.priceLabel,
                              currencyFormatter.format(detail.price),
                              highlight: true,
                            ),
                            const SizedBox(height: 12),
                            _buildDetailRow(
                              AppLocalizations.of(context)!.startDateLabel,
                              dateFormatter.format(detail.startDate),
                            ),
                            const SizedBox(height: 12),
                            _buildDetailRow(
                              AppLocalizations.of(context)!.endDateLabel,
                              dateFormatter.format(detail.endDate),
                            ),
                          ],
                        ),
                      ),
                      if (!widget.hideActions) ...[
                        const SizedBox(height: AppTheme.sectionGap),
                      if (detail.isDraft)
                        AppFilledButton(
                          label: AppLocalizations.of(context)!.pay,
                          onPressed: () {
                            context.push(
                              '/payment/qr',
                              extra: <int>[int.tryParse(detail.id) ?? 0],
                            );
                          },
                        )
                      else if (detail.status == BookingStatus.pendingPayment)
                        AppFilledButton(
                          label: AppLocalizations.of(context)!.pendingPayment,
                          onPressed: null, // Vô hiệu hóa
                        )
                      else
                        AppFilledButton(
                          label: AppLocalizations.of(context)!.renewContract,
                          onPressed: () {
                            context.push(
                              '/customer/booking-detail/renew/${detail.id}',
                            );
                          },
                        ),
                      ],
                    ],
                  ),
                );
              },
            ),
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
