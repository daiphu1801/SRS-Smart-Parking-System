import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/widgets/package_widgets.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

class BookingDetailScreen extends StatefulWidget {
  final String bookingId;
  const BookingDetailScreen({super.key, required this.bookingId});

  @override
  State<BookingDetailScreen> createState() => _BookingDetailScreenState();
}

class _BookingDetailScreenState extends State<BookingDetailScreen> {
  /// IDs of DRAFT booking-details the user has ticked for checkout.
  final Set<String> _selectedDraftIds = {};

  /// Guards one-time auto-selection of all DRAFTs on first successful load.
  bool _hasInitializedDrafts = false;

  String _maskPhone(String phone) {
    final p = phone.trim();
    if (p.length <= 5) return p;
    final start = p.substring(0, 3);
    final end = p.substring(p.length - 2);
    final middleLen = p.length - 5;
    final middle = List.filled(middleLen, '*').join();
    return '$start$middle$end';
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<BookingViewModel>().fetchBookingById(widget.bookingId);
    });
  }

  // ── Draft selection helpers ─────────────────────────────────────────────

  List<BookingDetail> _draftDetails(Booking booking) =>
      booking.details.where((d) => d.isDraft).toList();

  double _selectedTotal(Booking booking) {
    return booking.details
        .where((d) => _selectedDraftIds.contains(d.id))
        .fold<double>(0, (sum, d) => sum + d.price);
  }

  void _initDraftSelections(Booking booking) {
    if (_hasInitializedDrafts) return;
    _hasInitializedDrafts = true;
    final drafts = _draftDetails(booking);
    if (drafts.isNotEmpty) {
      setState(() {
        _selectedDraftIds.addAll(drafts.map((d) => d.id));
      });
    }
  }

  void _toggleDraft(String id, bool selected) {
    setState(() {
      if (selected) {
        _selectedDraftIds.add(id);
      } else {
        _selectedDraftIds.remove(id);
      }
    });
  }

  void _toggleAllDrafts(Booking booking, bool selectAll) {
    setState(() {
      final draftIds = _draftDetails(booking).map((d) => d.id);
      if (selectAll) {
        _selectedDraftIds.addAll(draftIds);
      } else {
        _selectedDraftIds.removeAll(draftIds);
      }
    });
  }

  Future<void> _paySelectedDrafts(Booking booking) async {
    final ids = booking.details
        .where((d) => _selectedDraftIds.contains(d.id))
        .map((d) => int.tryParse(d.id) ?? 0)
        .toList();
    if (ids.isEmpty) return;

    final result = await context.push<bool>('/payment/qr', extra: ids);
    if (result == true && mounted) {
      _hasInitializedDrafts = false;
      _selectedDraftIds.clear();
      context.read<BookingViewModel>().fetchBookingById(widget.bookingId);
    }
  }

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
        title: Text(AppLocalizations.of(context)!.monthlyPackageDetails),
      ),
      body: Consumer<BookingViewModel>(
          builder: (context, vm, child) {
            return switch (vm.currentBookingState) {
              Idle() ||
              Loading() => const Center(child: CircularProgressIndicator()),
              Failure(message: var msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: AppLocalizations.of(context)!.dataLoadError,
                subtitle: msg,
              ),
              Success(data: var booking) => Builder(
                builder: (context) {

                  final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');
                  final drafts = _draftDetails(booking);
                  final hasDrafts = drafts.isNotEmpty;
                  final pendingCount = booking.details.where((d) => d.isPendingPayment).length;
                  final hasPending = pendingCount > 0;

                  // Auto-select all drafts on first load
                  WidgetsBinding.instance.addPostFrameCallback((_) {
                    _initDraftSelections(booking);
                  });

                  final allDraftsSelected = hasDrafts &&
                      drafts.every(
                          (d) => _selectedDraftIds.contains(d.id));

                  return Column(
                    children: [
                      Expanded(
                        child: SingleChildScrollView(
                          padding:
                              const EdgeInsets.all(AppTheme.pagePadding),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              // ── Tổng quan booking ──────────────────
                              AppCard(
                                padding: const EdgeInsets.all(16),
                                child: Column(
                                  crossAxisAlignment:
                                      CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      mainAxisAlignment:
                                          MainAxisAlignment.spaceBetween,
                                      children: [
                                        Column(
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          children: [
                                            Text(
                                              '${AppLocalizations.of(context)!.contract} ${booking.id}',
                                              style: AppTheme.heading3,
                                            ),
                                            const SizedBox(height: 4),
                                            Text(
                                              AppLocalizations.of(context)!
                                                  .vehicleManagement,
                                              style: AppTheme.bodySmall
                                                  .copyWith(
                                                color: AppTheme.subtle,
                                              ),
                                            ),
                                          ],
                                        ),
                                        AppBadge(
                                          label: booking
                                              .paymentStatus.label,
                                          isFilled:
                                              booking.paymentStatus ==
                                              PaymentStatus.success,
                                        ),
                                      ],
                                    ),
                                    const Divider(height: 24),
                                    _buildInfoRow(
                                      AppLocalizations.of(context)!
                                          .groupRepresentative,
                                      booking.groupName,
                                    ),
                                    const SizedBox(height: 8),
                                    _buildInfoRow(
                                      AppLocalizations.of(context)!
                                          .createdAt,
                                      dateFormatter
                                          .format(booking.createdAt),
                                    ),
                                    const SizedBox(height: 8),
                                    _buildInfoRow(
                                      AppLocalizations.of(context)!
                                          .registeredVehiclesCount,
                                      '${booking.totalVehicles} ${AppLocalizations.of(context)!.vehicles}',
                                    ),
                                    const SizedBox(height: 8),
                                    if (booking.paymentMethod !=
                                        null) ...[
                                      _buildInfoRow(
                                        AppLocalizations.of(context)!
                                            .paymentMethod,
                                        booking.paymentMethod!.label,
                                      ),
                                      const SizedBox(height: 8),
                                    ],
                                    const SizedBox(height: 12),
                                    SizedBox(
                                      width: double.infinity,
                                      child: OutlinedButton.icon(
                                        onPressed: () async {
                                          final result = await context.push<bool>(
                                            '/customer/bookings/${booking.id}/add-vehicle',
                                          );
                                          if (result == true && mounted) {
                                            _hasInitializedDrafts = false;
                                            _selectedDraftIds.clear();
                                            vm.fetchBookingById(widget.bookingId);
                                          }
                                        },
                                        icon: const Icon(
                                            Icons.add_circle_outline),
                                        label: Text(
                                            AppLocalizations.of(context)!
                                                .addVehicleToBooking),
                                      ),
                                    ),
                                  ],
                                ),
                              ),

                              const SizedBox(height: AppTheme.sectionGap),

                              if (hasPending) ...[
                                PendingPaymentAlertBanner(count: pendingCount),
                                const SizedBox(height: AppTheme.sectionGap),
                              ],

                              // ── Danh sách booking detail ───────────
                              Text(
                                AppLocalizations.of(context)!
                                    .bookingDetailList,
                                style: AppTheme.heading3,
                              ),

                              // ── Select-all row (visible only when DRAFTs exist) ──
                              if (hasDrafts) ...[
                                const SizedBox(height: 8),
                                _DraftSelectAllRow(
                                  allSelected: allDraftsSelected,
                                  draftCount: drafts.length,
                                  onToggle: (val) =>
                                      _toggleAllDrafts(booking, val),
                                ),
                              ],

                              const SizedBox(height: 12),
                              ListView.separated(
                                shrinkWrap: true,
                                physics:
                                    const NeverScrollableScrollPhysics(),
                                itemCount: booking.details.length,
                                separatorBuilder: (context, index) =>
                                    const SizedBox(height: 12),
                                itemBuilder: (context, index) {
                                  final detail = booking.details[index];
                                  return BookingDetailCard(
                                    detail: detail,
                                    isSelected: detail.isDraft
                                        ? _selectedDraftIds
                                            .contains(detail.id)
                                        : null,
                                    onSelectedChanged: detail.isDraft
                                        ? (val) => _toggleDraft(
                                            detail.id, val ?? false)
                                        : null,
                                  );
                                },
                              ),

                              const SizedBox(height: AppTheme.sectionGap),

                            ],
                          ),
                        ),
                      ),

                      // ── Sticky Bottom Checkout Bar ──
                      if (_selectedDraftIds.isNotEmpty)
                        _CheckoutBottomBar(
                          selectedCount: _selectedDraftIds.length,
                          totalAmount: _selectedTotal(booking),
                          currencyFormatter: currencyFormatter,
                          onCheckout: () =>
                              _paySelectedDrafts(booking),
                        ),
                    ],
                  );
                },
              ),
            };
          },
        ),
    );
  }

  Widget _buildInfoRow(String label, String value, {bool isTotal = false}) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
        Expanded(
          child: Text(
            value,
            textAlign: TextAlign.right,
            style: isTotal
                ? AppTheme.heading3.copyWith(
                    fontSize: 15,
                    color: AppTheme.primary,
                  )
                : AppTheme.body,
          ),
        ),
      ],
    );
  }
}

// ─── Private widgets ─────────────────────────────────────────────────────────

/// Row with a "Select all drafts" checkbox.
class _DraftSelectAllRow extends StatelessWidget {
  final bool allSelected;
  final int draftCount;
  final ValueChanged<bool> onToggle;

  const _DraftSelectAllRow({
    required this.allSelected,
    required this.draftCount,
    required this.onToggle,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () => onToggle(!allSelected),
      borderRadius: BorderRadius.circular(AppTheme.radiusCard),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: AppTheme.primary.withValues(alpha: 0.05),
          borderRadius: BorderRadius.circular(AppTheme.radiusCard),
          border: Border.all(color: AppTheme.border),
        ),
        child: Row(
          children: [
            SizedBox(
              width: 24,
              height: 24,
              child: Checkbox(
                value: allSelected,
                onChanged: (val) => onToggle(val ?? false),
                activeColor: AppTheme.primary,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(4),
                ),
                materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                visualDensity: VisualDensity.compact,
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                AppLocalizations.of(context)!.selectAllDraftsCount(draftCount),
                style: AppTheme.body.copyWith(fontWeight: FontWeight.w500),
              ),
            ),
            AppBadge(
              label: AppLocalizations.of(context)!.draftsCountBadge(draftCount),
              color: Colors.orange,
            ),
          ],
        ),
      ),
    );
  }
}

/// Sticky bottom bar showing total and checkout button.
class _CheckoutBottomBar extends StatelessWidget {
  final int selectedCount;
  final double totalAmount;
  final NumberFormat currencyFormatter;
  final VoidCallback onCheckout;

  const _CheckoutBottomBar({
    required this.selectedCount,
    required this.totalAmount,
    required this.currencyFormatter,
    required this.onCheckout,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
      decoration: BoxDecoration(
        color: AppTheme.background,
        border: Border(top: BorderSide(color: AppTheme.border)),
        boxShadow: [
          BoxShadow(
            color: AppTheme.primary.withValues(alpha: 0.06),
            blurRadius: 10,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: Row(
          children: [
            // ── Amount info ──
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    AppLocalizations.of(context)!.total,
                    style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    currencyFormatter.format(totalAmount),
                    style: AppTheme.heading3.copyWith(
                      color: AppTheme.primary,
                      fontSize: 17,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            // ── Checkout button ──
            AppFilledButton(
              width: 170,
              label: AppLocalizations.of(context)!.checkoutWithCount(selectedCount),
              onPressed: onCheckout,
            ),
          ],
        ),
      ),
    );
  }
}
