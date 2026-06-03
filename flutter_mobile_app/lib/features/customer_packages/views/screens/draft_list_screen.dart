import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/widgets/package_widgets.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';

class DraftListScreen extends StatefulWidget {
  const DraftListScreen({super.key});

  @override
  State<DraftListScreen> createState() => _DraftListScreenState();
}

class _DraftListScreenState extends State<DraftListScreen> {
  final Set<String> _selectedIds = {};

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _fetchData();
    });
  }

  Future<void> _fetchData() async {
    await context.read<BookingViewModel>().fetchDrafts();
  }

  void _toggleSelection(String id, bool selected) {
    setState(() {
      if (selected) {
        _selectedIds.add(id);
      } else {
        _selectedIds.remove(id);
      }
    });
  }

  void _toggleAll(List<BookingDetail> details, bool selectAll) {
    setState(() {
      if (selectAll) {
        _selectedIds.addAll(details.map((d) => d.id));
      } else {
        _selectedIds.clear();
      }
    });
  }

  double _calculateTotal(List<BookingDetail> details) {
    return details
        .where((d) => _selectedIds.contains(d.id))
        .fold(0.0, (sum, d) => sum + d.price);
  }


  Future<void> _checkout() async {
    final ids = _selectedIds.map((id) => int.tryParse(id) ?? 0).toList();
    if (ids.isEmpty) return;

    final result = await context.push<bool>('/payment/qr', extra: ids);
    if (result == true && mounted) {
      _selectedIds.clear();
      _fetchData();
    }
  }

  Future<void> _deleteSelected() async {
    if (_selectedIds.isEmpty) return;

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Xóa giỏ hàng'),
        content: const Text('Bạn có chắc chắn muốn xóa các xe đã chọn khỏi giỏ hàng?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Hủy', style: TextStyle(color: Colors.grey)),
          ),
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            child: const Text('Xóa', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    try {
      if (!mounted) return;
      await context.read<BookingViewModel>().deleteSelectedDrafts(_selectedIds.toList());
      _selectedIds.clear();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Đã xóa thành công')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Lỗi khi xóa: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final currencyFormatter = NumberFormat.currency(locale: 'vi_VN', symbol: '₫');

    return Scaffold(
      appBar: AppBar(
        leading: context.canPop()
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => context.pop(),
              )
            : null,
        title: const Text('Giỏ hàng'),
      ),
      body: Consumer<BookingViewModel>(
        builder: (context, vm, child) {
          final state = vm.draftsState;

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

          if (state is Success) {
            final dtos = (state as Success).data as List<BookingDetailDto>;
            final details = dtos.map((dto) => BookingDetail.fromDto(dto)).toList();

            if (details.isEmpty) {
              return const AppEmptyState(
                icon: Icons.directions_car_outlined,
                title: 'Giỏ hàng trống',
                subtitle: 'Hiện tại chưa có phương tiện nào trong giỏ hàng.',
              );
            }

            final allSelected = details.isNotEmpty && _selectedIds.length == details.length;

            return Column(
              children: [
                Expanded(
                  child: RefreshIndicator(
                    color: AppTheme.primary,
                    onRefresh: () async {
                      _selectedIds.clear();
                      await _fetchData();
                    },
                    child: ListView.builder(
                      padding: const EdgeInsets.all(AppTheme.pagePadding),
                      itemCount: details.length + 1,
                      itemBuilder: (context, index) {
                        if (index == 0) {
                          return Padding(
                            padding: const EdgeInsets.only(bottom: 12),
                            child: _DraftSelectAllRow(
                              allSelected: allSelected,
                              draftCount: details.length,
                              onToggle: (val) => _toggleAll(details, val),
                            ),
                          );
                        }

                        final detail = details[index - 1];
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 12),
                          child: BookingDetailCard(
                            key: ValueKey('draft_${detail.id}'),
                            detail: detail,
                            isSelected: _selectedIds.contains(detail.id),
                            onSelectedChanged: (val) => _toggleSelection(detail.id, val ?? false),
                          ),
                        );
                      },
                    ),
                  ),
                ),
                _CheckoutBottomBar(
                  selectedCount: _selectedIds.length,
                  totalAmount: _calculateTotal(details),
                  currencyFormatter: currencyFormatter,
                  onCheckout: _selectedIds.isEmpty ? null : _checkout,
                  onDelete: _selectedIds.isEmpty ? null : _deleteSelected,
                ),
              ],
            );
          }

          return const SizedBox.shrink();
        },
      ),
    );
  }
}

// ─── Private widgets ─────────────────────────────────────────────────────────

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
    return GestureDetector(
      onTap: () => onToggle(!allSelected),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: AppTheme.primary.withValues(alpha: 0.05),
          borderRadius: BorderRadius.circular(AppTheme.radiusCard),
          border: Border.all(color: AppTheme.border),
        ),
        child: Row(
          children: [
            Checkbox(
              value: allSelected,
              onChanged: (val) => onToggle(val ?? false),
              activeColor: AppTheme.primary,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(4),
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                'Chọn tất cả ($draftCount)',
                style: AppTheme.body.copyWith(fontWeight: FontWeight.w500),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _CheckoutBottomBar extends StatelessWidget {
  final int selectedCount;
  final double totalAmount;
  final NumberFormat currencyFormatter;
  final VoidCallback? onCheckout;
  final VoidCallback? onDelete;

  const _CheckoutBottomBar({
    required this.selectedCount,
    required this.totalAmount,
    required this.currencyFormatter,
    required this.onCheckout,
    required this.onDelete,
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
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    'Tổng cộng',
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
            OutlinedButton(
              onPressed: onDelete,
              style: OutlinedButton.styleFrom(
                minimumSize: Size.zero,
                foregroundColor: Colors.red,
                side: BorderSide(color: onDelete == null ? Colors.grey : Colors.red),
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              ),
              child: Text('Xóa', style: TextStyle(color: onDelete == null ? Colors.grey : Colors.red)),
            ),
            const SizedBox(width: 8),
            AppFilledButton(
              width: 140,
              label: 'Thanh toán',
              onPressed: onCheckout,
              backgroundColor: onCheckout == null ? Colors.grey : AppTheme.primary,
            ),
          ],
        ),
      ),
    );
  }
}
