import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/viewmodels/parking_session_viewmodel.dart';

/// Screen: History — parking sessions & payments tabs
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
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Lịch sử', style: AppTheme.heading1),
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: AppTheme.primary,
          labelColor: AppTheme.background,
          labelStyle: AppTheme.label,
          unselectedLabelColor: AppTheme.primary,
          indicator: BoxDecoration(
            color: AppTheme.primary,
            borderRadius: BorderRadius.circular(AppTheme.radiusButton),
          ),
          tabs: const [Tab(text: 'Lượt đỗ xe'), Tab(text: 'Thanh toán')],
          splashBorderRadius: BorderRadius.circular(AppTheme.radiusButton),
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: const [
          _SessionList(),
          _PaymentList(),
        ],
      ),
    );
  }
}

// ── SESSION LIST & FILTERS ────────────────────────────────────────────────
class _SessionList extends StatefulWidget {
  const _SessionList();

  @override
  State<_SessionList> createState() => _SessionListState();
}

class _SessionListState extends State<_SessionList> {
  DateTime? _startDate;
  DateTime? _endDate;
  String? _plateNumber;

  @override
  void initState() {
    super.initState();
    _fetchData();
  }

  void _fetchData() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ParkingSessionViewModel>().fetchSessionHistory(
            customerId: 'CUST-001',
            startDate: _startDate,
            endDate: _endDate,
            plateNumber: _plateNumber,
          );
    });
  }

  Future<void> _showFilterBottomSheet() async {
    final result = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppTheme.background,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) => _FilterBottomSheet(
        initialStartDate: _startDate,
        initialEndDate: _endDate,
        initialPlateNumber: _plateNumber,
      ),
    );

    if (result != null) {
      setState(() {
        _startDate = result['startDate'];
        _endDate = result['endDate'];
        _plateNumber = result['plateNumber'];
      });
      _fetchData();
    }
  }

  @override
  Widget build(BuildContext context) {
    final currencyFormatter = NumberFormat.currency(locale: 'vi_VN', symbol: 'đ');
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');

    return Column(
      children: [
        // Filter Bar
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppTheme.pagePadding, vertical: 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Lịch sử đỗ xe',
                style: AppTheme.heading3,
              ),
              TextButton.icon(
                onPressed: _showFilterBottomSheet,
                icon: const Icon(Icons.filter_list, size: 20),
                label: const Text('Lọc'),
                style: TextButton.styleFrom(
                  foregroundColor: AppTheme.primary,
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  backgroundColor: AppTheme.primary.withValues(alpha: 0.1),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                ),
              ),
            ],
          ),
        ),
        
        // Active Filters Display (Optional)
        if (_startDate != null || _endDate != null || (_plateNumber != null && _plateNumber!.isNotEmpty))
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: AppTheme.pagePadding).copyWith(bottom: 8),
            child: Row(
              children: [
                Icon(Icons.check_circle, size: 16, color: AppTheme.primary),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Đang áp dụng bộ lọc',
                    style: AppTheme.bodySmall.copyWith(color: AppTheme.primary, fontWeight: FontWeight.w600),
                  ),
                ),
                InkWell(
                  onTap: () {
                    setState(() {
                      _startDate = null;
                      _endDate = null;
                      _plateNumber = null;
                    });
                    _fetchData();
                  },
                  child: Text('Xóa lọc', style: AppTheme.bodySmall.copyWith(color: Colors.red)),
                )
              ],
            ),
          ),

        // List View
        Expanded(
          child: Consumer<ParkingSessionViewModel>(
            builder: (context, vm, _) {
              final state = vm.historySessionsState;

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
                  onRefresh: () async => _fetchData(),
                  child: ListView.separated(
                    padding: const EdgeInsets.all(AppTheme.pagePadding),
                    itemCount: sessions.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 12),
                    itemBuilder: (context, i) {
                      final s = sessions[i];
                      
                      return AppCard(
                        onTap: () => context.push(AppRoutes.parkingSessionDetail.replaceAll(':id', s.id)),
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Row(
                                  children: [
                                    Icon(
                                      s.vehicleType == 'CAR' ? Icons.directions_car : Icons.two_wheeler,
                                      color: AppTheme.primary,
                                      size: 20,
                                    ),
                                    const SizedBox(width: 8),
                                    Text(s.plateNumber, style: AppTheme.heading3.copyWith(fontSize: 16)),
                                  ],
                                ),
                                AppBadge(
                                  label: s.isPaid ? 'Đã thanh toán' : 'Chưa thanh toán',
                                  isFilled: s.isPaid,
                                ),
                              ],
                            ),
                            const Divider(height: 24),
                            Row(
                              children: [
                                Icon(Icons.login, size: 16, color: AppTheme.subtle),
                                const SizedBox(width: 8),
                                Expanded(
                                  child: Text(
                                    'Vào: ${dateFormatter.format(s.entryTime)}',
                                    style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
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
                                    s.exitTime != null ? 'Ra: ${dateFormatter.format(s.exitTime!)}' : 'Chưa ra',
                                    style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Text(
                                  s.durationFormatted,
                                  style: AppTheme.body.copyWith(fontWeight: FontWeight.w600),
                                ),
                                Text(
                                  currencyFormatter.format(s.amountDue),
                                  style: AppTheme.heading3.copyWith(color: AppTheme.primary, fontSize: 16),
                                ),
                              ],
                            ),
                          ],
                        ),
                      );
                    },
                  ),
                );
              }
              return const SizedBox.shrink();
            },
          ),
        ),
      ],
    );
  }
}

// ── FILTER BOTTOM SHEET ──────────────────────────────────────────────────
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
    _plateController = TextEditingController(text: widget.initialPlateNumber);
  }

  @override
  void dispose() {
    _plateController.dispose();
    super.dispose();
  }

  Future<void> _selectDate(BuildContext context, bool isStart) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: isStart ? (_startDate ?? DateTime.now()) : (_endDate ?? DateTime.now()),
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.light(
              primary: AppTheme.primary,
              onPrimary: Colors.white,
              onSurface: Colors.black,
            ),
          ),
          child: child!,
        );
      },
    );
    if (picked != null) {
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
          
          Text('Khoảng thời gian', style: AppTheme.heading3),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: InkWell(
                  onTap: () => _selectDate(context, true),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
                    decoration: BoxDecoration(
                      border: Border.all(color: AppTheme.border),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          _startDate != null ? dateFormatter.format(_startDate!) : 'Từ ngày',
                          style: _startDate != null ? AppTheme.body : AppTheme.body.copyWith(color: AppTheme.subtle),
                        ),
                        Icon(Icons.calendar_today, size: 18, color: AppTheme.subtle),
                      ],
                    ),
                  ),
                ),
              ),
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 12),
                child: Icon(Icons.arrow_forward_outlined, size: 20, color: Colors.grey),
              ),
              Expanded(
                child: InkWell(
                  onTap: () => _selectDate(context, false),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
                    decoration: BoxDecoration(
                      border: Border.all(color: AppTheme.border),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          _endDate != null ? dateFormatter.format(_endDate!) : 'Đến ngày',
                          style: _endDate != null ? AppTheme.body : AppTheme.body.copyWith(color: AppTheme.subtle),
                        ),
                        Icon(Icons.calendar_today, size: 18, color: AppTheme.subtle),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),

          Text('Biển số xe', style: AppTheme.heading3),
          const SizedBox(height: 12),
          TextFormField(
            controller: _plateController,
            decoration: const InputDecoration(
              hintText: 'Nhập biển số xe (VD: 30A)',
              border: OutlineInputBorder(),
              contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 14),
            ),
          ),
          
          const SizedBox(height: 32),
          Row(
            children: [
              Expanded(
                child: AppOutlinedButton(
                  label: 'Thiết lập lại',
                  onPressed: () {
                    setState(() {
                      _startDate = null;
                      _endDate = null;
                      _plateController.clear();
                    });
                  },
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: AppFilledButton(
                  label: 'Áp dụng',
                  onPressed: () {
                    Navigator.pop(context, {
                      'startDate': _startDate,
                      'endDate': _endDate,
                      'plateNumber': _plateController.text,
                    });
                  },
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

// ── PAYMENT LIST (MOCK) ──────────────────────────────────────────────────
class _PaymentList extends StatelessWidget {
  const _PaymentList();

  final List<Map<String, String>> payments = const [
    {'code': 'PAY_001', 'amount': '200,000 đ', 'method': 'QR', 'date': '01/04/2026'},
    {'code': 'PAY_002', 'amount': '25,000 đ', 'method': 'Cash', 'date': '23/04/2026'},
  ];

  @override
  Widget build(BuildContext context) {
    return RefreshIndicator(
      color: AppTheme.primary,
      onRefresh: () async => await Future.delayed(const Duration(seconds: 1)),
      child: ListView.separated(
        padding: const EdgeInsets.all(AppTheme.pagePadding),
        itemCount: payments.length,
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemBuilder: (_, i) {
          final p = payments[i];
          return AppCard(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(p['code']!, style: AppTheme.heading3.copyWith(fontSize: 16)),
                    const SizedBox(height: 4),
                    Text('${p['date']} · ${p['method']}',
                        style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                  ],
                ),
                Text(
                  p['amount']!,
                  style: AppTheme.heading3.copyWith(color: AppTheme.primary, fontSize: 16),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
