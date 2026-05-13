import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/features/customer_history/models/history_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/features/customer_history/viewmodels/history_viewmodel.dart';

/// Màn hình Lịch sử — theo chuẩn MVVM của dự án.
/// View chỉ lắng nghe [HistoryViewModel], không chứa business logic.
class CustomerHistoryScreen extends StatefulWidget {
  const CustomerHistoryScreen({super.key});

  @override
  State<CustomerHistoryScreen> createState() => _CustomerHistoryScreenState();
}

class _CustomerHistoryScreenState extends State<CustomerHistoryScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<HistoryViewModel>().fetchHistory(customerId: 'CUST-001');
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Lịch sử', style: AppTheme.heading1),
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(52),
          child: Container(
            margin: const EdgeInsets.fromLTRB(16, 0, 16, 12),
            height: 40,
            decoration: BoxDecoration(
              color: AppTheme.primary.withValues(alpha: 0.08),
              borderRadius: BorderRadius.circular(20),
            ),
            child: TabBar(
              controller: _tabController,
              labelColor: Colors.white,
              labelStyle: AppTheme.label,
              unselectedLabelColor: AppTheme.primary,
              unselectedLabelStyle: AppTheme.label,
              dividerColor: Colors.transparent,
              indicator: BoxDecoration(
                color: AppTheme.primary,
                borderRadius: BorderRadius.circular(20),
              ),
              indicatorSize: TabBarIndicatorSize.tab,
              overlayColor: WidgetStatePropertyAll(Colors.transparent),
              tabs: const [
                Tab(text: 'Lượt đỗ xe'),
                Tab(text: 'Thanh toán'),
              ],
            ),
          ),
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: const [
          _SessionHistoryTab(),
          _PaymentTab(),
        ],
      ),
    );
  }
}

// ─── Tab 1: Lịch sử Phiên đỗ xe ─────────────────────────────────────────────

class _SessionHistoryTab extends StatelessWidget {
  const _SessionHistoryTab();

  @override
  Widget build(BuildContext context) {
    return Consumer<HistoryViewModel>(
      builder: (context, vm, _) {
        return Column(
          children: [
            _FilterBar(vm: vm),
            if (vm.hasActiveFilter) _ActiveFilterBadge(vm: vm),
            Expanded(child: _HistoryList(vm: vm)),
          ],
        );
      },
    );
  }
}

// ── Filter Bar ────────────────────────────────────────────────────────────────

class _FilterBar extends StatelessWidget {
  final HistoryViewModel vm;
  const _FilterBar({required this.vm});

  Future<void> _openFilterSheet(BuildContext context) async {
    final result = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppTheme.background,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => _FilterBottomSheet(
        initialStartDate: vm.startDate,
        initialEndDate: vm.endDate,
        initialPlateNumber: vm.plateNumber,
      ),
    );

    if (result != null && context.mounted) {
      await vm.applyFilter(
        customerId: 'CUST-001',
        newStartDate: result['startDate'],
        newEndDate: result['endDate'],
        newPlateNumber: result['plateNumber'],
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(
          horizontal: AppTheme.pagePadding, vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text('Lịch sử đỗ xe', style: AppTheme.heading3),
          TextButton.icon(
            onPressed: () => _openFilterSheet(context),
            icon: const Icon(Icons.filter_list, size: 20),
            label: const Text('Lọc'),
            style: TextButton.styleFrom(
              foregroundColor: AppTheme.primary,
              padding:
                  const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              backgroundColor: AppTheme.primary.withValues(alpha: 0.1),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16)),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Active Filter Badge ───────────────────────────────────────────────────────

class _ActiveFilterBadge extends StatelessWidget {
  final HistoryViewModel vm;
  const _ActiveFilterBadge({required this.vm});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: AppTheme.pagePadding)
          .copyWith(bottom: 8),
      child: Row(
        children: [
          Icon(Icons.check_circle, size: 16, color: AppTheme.primary),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              'Đang áp dụng bộ lọc',
              style: AppTheme.bodySmall.copyWith(
                  color: AppTheme.primary, fontWeight: FontWeight.w600),
            ),
          ),
          InkWell(
            onTap: () => vm.clearFilter(customerId: 'CUST-001'),
            child: Text('Xóa lọc',
                style: AppTheme.bodySmall.copyWith(color: Colors.red)),
          ),
        ],
      ),
    );
  }
}

// ── History List ──────────────────────────────────────────────────────────────

class _HistoryList extends StatelessWidget {
  final HistoryViewModel vm;
  const _HistoryList({required this.vm});

  @override
  Widget build(BuildContext context) {
    final state = vm.historyState;

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

    if (state is Success<List<ParkingSession>>) {
      final sessions = state.data;

      if (sessions.isEmpty) {
        return const AppEmptyState(
          icon: Icons.history,
          title: 'Không tìm thấy lịch sử đỗ xe',
          subtitle: 'Thử thay đổi điều kiện lọc của bạn.',
        );
      }

      return RefreshIndicator(
        color: AppTheme.primary,
        onRefresh: () => vm.fetchHistory(customerId: 'CUST-001'),
        child: ListView.separated(
          padding: const EdgeInsets.all(AppTheme.pagePadding),
          itemCount: sessions.length,
          separatorBuilder: (_, __) => const SizedBox(height: 12),
          itemBuilder: (context, i) =>
              _SessionCard(session: sessions[i]),
        ),
      );
    }

    return const SizedBox.shrink();
  }
}

// ── Session Card ──────────────────────────────────────────────────────────────

class _SessionCard extends StatelessWidget {
  final ParkingSession session;
  const _SessionCard({required this.session});

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');
    final currencyFormatter =
        NumberFormat.currency(locale: 'vi_VN', symbol: 'đ');

    return AppCard(
      onTap: () => context.push(
          AppRoutes.parkingSessionDetail.replaceAll(':id', session.id)),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header: biển số + badge thanh toán
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(
                    session.vehicleType == 'CAR'
                        ? Icons.directions_car
                        : Icons.two_wheeler,
                    color: AppTheme.primary,
                    size: 20,
                  ),
                  const SizedBox(width: 8),
                  Text(session.plateNumber,
                      style: AppTheme.heading3.copyWith(fontSize: 16)),
                ],
              ),
              AppBadge(
                label: session.isPaid ? 'Đã thanh toán' : 'Chưa trả',
                isFilled: true,
                color: session.isPaid ? Colors.green : Colors.orange.shade700,
              ),
            ],
          ),
          const Divider(height: 24),
          // Thời gian vào / ra
          Row(
            children: [
              Icon(Icons.login, size: 16, color: AppTheme.subtle),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  'Vào: ${dateFormatter.format(session.entryTime)}',
                  style:
                      AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              Icon(Icons.logout, size: 16, color: AppTheme.subtle),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  session.exitTime != null
                      ? 'Ra: ${dateFormatter.format(session.exitTime!)}'
                      : 'Chưa ra',
                  style:
                      AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          // Footer: thời lượng + phí
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(session.durationFormatted,
                  style:
                      AppTheme.body.copyWith(fontWeight: FontWeight.w600)),
              Text(
                currencyFormatter.format(session.amountDue),
                style: AppTheme.heading3
                    .copyWith(color: AppTheme.primary, fontSize: 16),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

// ─── Filter Bottom Sheet ──────────────────────────────────────────────────────

class _FilterBottomSheet extends StatefulWidget {
  final DateTime? initialStartDate;
  final DateTime? initialEndDate;
  final String? initialPlateNumber;

  const _FilterBottomSheet({
    this.initialStartDate,
    this.initialEndDate,
    this.initialPlateNumber,
  });

  @override
  State<_FilterBottomSheet> createState() => _FilterBottomSheetState();
}

class _FilterBottomSheetState extends State<_FilterBottomSheet> {
  DateTime? _startDate;
  DateTime? _endDate;
  late TextEditingController _plateController;

  @override
  void initState() {
    super.initState();
    _startDate = widget.initialStartDate;
    _endDate = widget.initialEndDate;
    _plateController =
        TextEditingController(text: widget.initialPlateNumber ?? '');
  }

  @override
  void dispose() {
    _plateController.dispose();
    super.dispose();
  }

  Future<void> _pickDate(bool isStart) async {
    final picked = await showDatePicker(
      context: context,
      initialDate:
          isStart ? (_startDate ?? DateTime.now()) : (_endDate ?? DateTime.now()),
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
      builder: (ctx, child) => Theme(
        data: Theme.of(ctx).copyWith(
          colorScheme: const ColorScheme.light(
            primary: AppTheme.primary,
            onPrimary: Colors.white,
            onSurface: Colors.black,
          ),
        ),
        child: child!,
      ),
    );
    if (picked == null) return;
    setState(() {
      if (isStart) {
        _startDate = picked;
        if (_endDate != null && _endDate!.isBefore(_startDate!)) {
          _endDate = _startDate;
        }
      } else {
        _endDate = picked;
        if (_startDate != null && _startDate!.isAfter(_endDate!)) {
          _startDate = _endDate;
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy');

    return Padding(
      padding: EdgeInsets.only(
        left: 24,
        right: 24,
        top: 24,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Bộ lọc tìm kiếm', style: AppTheme.heading2),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Khoảng thời gian
          Text('Khoảng thời gian', style: AppTheme.heading3),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(child: _DatePickerField(
                label: _startDate != null
                    ? dateFormatter.format(_startDate!)
                    : 'Từ ngày',
                onTap: () => _pickDate(true),
              )),
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 12),
                child: Icon(Icons.arrow_forward, size: 20, color: Colors.grey),
              ),
              Expanded(child: _DatePickerField(
                label: _endDate != null
                    ? dateFormatter.format(_endDate!)
                    : 'Đến ngày',
                onTap: () => _pickDate(false),
              )),
            ],
          ),
          const SizedBox(height: 24),

          // Biển số xe
          Text('Biển số xe', style: AppTheme.heading3),
          const SizedBox(height: 12),
          TextFormField(
            controller: _plateController,
            decoration: const InputDecoration(
              hintText: 'VD: 30A-123.45',
              border: OutlineInputBorder(),
              contentPadding:
                  EdgeInsets.symmetric(horizontal: 12, vertical: 14),
            ),
          ),
          const SizedBox(height: 32),

          // Actions
          Row(
            children: [
              Expanded(
                child: AppOutlinedButton(
                  label: 'Thiết lập lại',
                  onPressed: () => setState(() {
                    _startDate = null;
                    _endDate = null;
                    _plateController.clear();
                  }),
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: AppFilledButton(
                  label: 'Áp dụng',
                  onPressed: () => Navigator.pop(context, {
                    'startDate': _startDate,
                    'endDate': _endDate,
                    'plateNumber': _plateController.text,
                  }),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _DatePickerField extends StatelessWidget {
  final String label;
  final VoidCallback onTap;
  const _DatePickerField({required this.label, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding:
            const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
        decoration: BoxDecoration(
          border: Border.all(color: AppTheme.border),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Text(
                label,
                style: AppTheme.body.copyWith(
                  color: label.contains('/') ? null : AppTheme.subtle,
                ),
                overflow: TextOverflow.ellipsis,
              ),
            ),
            const SizedBox(width: 4),
            Icon(Icons.calendar_today, size: 16, color: AppTheme.subtle),
          ],
        ),
      ),
    );
  }
}

// ─── Tab 2: Lịch sử Thanh toán ──────────────────────────────────────────────

class _PaymentTab extends StatefulWidget {
  const _PaymentTab();

  @override
  State<_PaymentTab> createState() => _PaymentTabState();
}

class _PaymentTabState extends State<_PaymentTab> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<HistoryViewModel>().fetchPayments(customerId: 'CUST-001');
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<HistoryViewModel>(
      builder: (context, vm, _) {
        final state = vm.paymentState;

        if (state is Loading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (state is Failure) {
          return AppEmptyState(
            icon: Icons.error_outline,
            title: 'Không thể tải lịch sử thanh toán',
            subtitle: (state as Failure).message,
          );
        }

        if (state is Success<List<PaymentTransaction>>) {
          final payments = state.data;

          if (payments.isEmpty) {
            return const AppEmptyState(
              icon: Icons.receipt_long_outlined,
              title: 'Chưa có giao dịch nào',
              subtitle: 'Các giao dịch thanh toán sẽ xuất hiện tại đây.',
            );
          }

          return RefreshIndicator(
            color: AppTheme.primary,
            onRefresh: () => vm.fetchPayments(customerId: 'CUST-001'),
            child: ListView.separated(
              padding: const EdgeInsets.all(AppTheme.pagePadding),
              itemCount: payments.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (_, i) => _PaymentCard(transaction: payments[i]),
            ),
          );
        }

        return const SizedBox.shrink();
      },
    );
  }
}

class _PaymentCard extends StatelessWidget {
  final PaymentTransaction transaction;
  const _PaymentCard({required this.transaction});

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');
    final currencyFormatter =
        NumberFormat.currency(locale: 'vi_VN', symbol: 'đ');

    return AppCard(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header: ID + badge trạng thái
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: AppTheme.primary.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Icon(
                        transaction.method == PaymentMethod.qr
                            ? Icons.qr_code_rounded
                            : Icons.payments_outlined,
                        color: AppTheme.primary,
                        size: 18,
                      ),
                    ),
                    const SizedBox(width: 10),
                    Flexible(
                      child: Text(
                        transaction.id,
                        style: AppTheme.heading3.copyWith(fontSize: 15),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: transaction.status.isSuccess
                      ? Colors.green.withValues(alpha: 0.12)
                      : Colors.red.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  transaction.status.label,
                  style: AppTheme.caption.copyWith(
                    color: transaction.status.isSuccess
                        ? Colors.green.shade700
                        : Colors.red.shade700,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
          const Divider(height: 20),
          // Target info
          Row(
            children: [
              Icon(Icons.info_outline, size: 14, color: AppTheme.subtle),
              const SizedBox(width: 6),
              Expanded(
                child: Text(
                  transaction.targetLabel,
                  style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              Icon(Icons.access_time, size: 14, color: AppTheme.subtle),
              const SizedBox(width: 6),
              Text(
                dateFormatter.format(transaction.createdAt),
                style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
              ),
            ],
          ),
          const SizedBox(height: 12),
          // Footer: phương thức + số tiền
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: AppTheme.surface,
                  borderRadius: BorderRadius.circular(6),
                  border: Border.all(color: AppTheme.border),
                ),
                child: Text(transaction.method.label,
                    style: AppTheme.caption.copyWith(color: AppTheme.subtle)),
              ),
              Text(
                currencyFormatter.format(transaction.amount),
                style: AppTheme.heading3
                    .copyWith(color: AppTheme.primary, fontSize: 16),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
