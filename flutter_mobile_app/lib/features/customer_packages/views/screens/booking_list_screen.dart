import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/widgets/package_widgets.dart';
import 'package:smart_parking_mobile/core/utils/language_viewmodel.dart';

class BookingListScreen extends StatefulWidget {
  const BookingListScreen({super.key});

  @override
  State<BookingListScreen> createState() => _BookingListScreenState();
}

class _BookingListScreenState extends State<BookingListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<BookingViewModel>().fetchBookings('CUST-001');
    });
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  List<BookingDetail> _draftDetails(Booking booking) =>
      booking.details.where((d) => d.isDraft).toList();

  // ── build ───────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    final currencyFormatter = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: '₫',
    );

    return Scaffold(
      appBar: AppBar(
        leading: context.canPop()
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => context.pop(),
              )
            : null,
        title: Text(AppLocalizations.of(context)!.monthlyContracts),
        actions: [
          Consumer<LanguageViewModel>(
            builder: (context, langVm, child) {
              final isVi = langVm.currentLocale.languageCode == 'vi';
              return Padding(
                padding: const EdgeInsets.only(right: 16),
                child: InkWell(
                  onTap: () {
                    final newLocale = isVi ? const Locale('en') : const Locale('vi');
                    langVm.changeLanguage(newLocale);
                  },
                  borderRadius: BorderRadius.circular(20),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppTheme.primary.withValues(alpha: 0.05),
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(
                        color: AppTheme.primary.withValues(alpha: 0.15),
                        width: 1,
                      ),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          isVi ? '🇻🇳 VI' : '🇬🇧 EN',
                          style: AppTheme.bodySmall.copyWith(
                            fontWeight: FontWeight.w600,
                            color: AppTheme.primary,
                          ),
                        ),
                        const SizedBox(width: 4),
                        const Icon(
                          Icons.translate_rounded,
                          size: 14,
                          color: AppTheme.primary,
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        ],
      ),
      body: Consumer<BookingViewModel>(
        builder: (context, vm, child) {
          return switch (vm.bookingsState) {
            Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
              icon: Icons.error_outline,
              title: AppLocalizations.of(context)!.dataLoadError,
              subtitle: msg,
            ),
            Success(data: var bookings) when bookings.isEmpty =>
              AppEmptyState(
                icon: Icons.receipt_long_outlined,
                title: AppLocalizations.of(context)!.noContractsYet,
                subtitle: AppLocalizations.of(context)!.monthlyPackageSavesMoney,
              ),
            Success(data: var bookings) => Builder(
              builder: (context) {
                final booking = bookings.first;
                final drafts = _draftDetails(booking);
                final hasDrafts = drafts.isNotEmpty;
                final pendingCount = booking.details.where((d) => d.isPendingPayment).length;
                final hasPending = pendingCount > 0;

                return Column(
                  children: [
                    Expanded(
                      child: RefreshIndicator(
                        color: AppTheme.primary,
                        onRefresh: () {
                          return vm.fetchBookings('CUST-001');
                        },
                        child: ListView(
                          padding: const EdgeInsets.all(AppTheme.pagePadding),
                          children: [
                            BookingOverviewHeader(booking: booking),
                            const SizedBox(height: 16),
                            Row(
                              children: [
                                Expanded(
                                  child: OutlinedButton.icon(
                                    onPressed: () {
                                      context.push('/customer/draft-list');
                                    },
                                    icon: const Icon(Icons.shopping_cart_outlined, size: 18),
                                    label: const Text('Xem giỏ hàng'),
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: OutlinedButton.icon(
                                    onPressed: () {
                                      context.push(
                                        '/customer/booking-details-list',
                                        extra: {'title': 'Lịch sử'},
                                      );
                                    },
                                    icon: const Icon(Icons.history, size: 18),
                                    label: const Text('Lịch sử'),
                                  ),
                                ),
                              ],
                            ),
                            if (hasPending) ...[
                              const SizedBox(height: AppTheme.sectionGap),
                              PendingPaymentAlertBanner(count: pendingCount),
                            ],
                            const SizedBox(height: AppTheme.sectionGap),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Text(AppLocalizations.of(context)!.vehicleList,
                                    style: AppTheme.heading3),
                                TextButton.icon(
                                  onPressed: () async {
                                    final vm = context.read<BookingViewModel>();
                                    final result = await context.push<bool>(
                                      '/customer/bookings/${booking.id}/add-vehicle',
                                    );
                                    if (result == true && mounted) {
                                      vm.fetchBookings('CUST-001');
                                    }
                                  },
                                  icon: const Icon(Icons.add, size: 18),
                                  label: Text(
                                      AppLocalizations.of(context)!.addVehicle),
                                ),
                              ],
                            ),

                            const SizedBox(height: 12),
                            if (booking.details.isEmpty)
                              AppEmptyState(
                                icon: Icons.directions_car_outlined,
                                title: AppLocalizations.of(context)!
                                    .noVehiclesYet,
                                subtitle: AppLocalizations.of(context)!
                                    .clickAddVehicleToStart,
                              )
                            else
                              ListView.separated(
                                shrinkWrap: true,
                                physics: const NeverScrollableScrollPhysics(),
                                itemCount: booking.details.length,
                                separatorBuilder: (context, index) =>
                                    const SizedBox(height: 12),
                                itemBuilder: (context, index) {
                                  final detail = booking.details[index];
                                  return BookingDetailCard(
                                    detail: detail,
                                  );
                                },
                              ),
                          ],
                        ),
                      ),
                    ),
                  ],
                );
              },
            ),
            _ => const SizedBox.shrink(),
          };
        },
      ),
    );
  }
}

