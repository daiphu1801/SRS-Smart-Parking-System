import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/features/customer_account/viewmodels/group_accounts_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_account/models/child_account.dart';
import 'package:smart_parking_mobile/features/customer_account/views/widgets/customer_widgets.dart';

class ChildAccountsScreen extends StatefulWidget {
  const ChildAccountsScreen({super.key});

  @override
  State<ChildAccountsScreen> createState() => _ChildAccountsScreenState();
}

class _ChildAccountsScreenState extends State<ChildAccountsScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      final bookingVm = context.read<BookingViewModel>();
      await bookingVm.fetchBookings('CUST-001');
      if (bookingVm.bookingsState is Success<List<Booking>>) {
        final list = (bookingVm.bookingsState as Success<List<Booking>>).data;
        if (list.isNotEmpty) {
          final booking = list.first;
          context.read<GroupAccountsViewModel>().fetchChildren(booking.id);
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Quản lý tài khoản con')),
      body: Consumer2<BookingViewModel, GroupAccountsViewModel>(
        builder: (context, bookingVm, groupVm, child) {
          final bState = bookingVm.bookingsState;
          if (bState is Loading) return const Center(child: CircularProgressIndicator());
          if (bState is Failure) {
            return AppEmptyState(
              icon: Icons.error_outline,
              title: 'Lỗi tải booking',
              subtitle: (bState as Failure).message,
            );
          }

          final bookings = switch (bState) {
            Success<List<Booking>> s => s.data,
            _ => <Booking>[],
          };
          if (bookings.isEmpty) {
            return AppEmptyState(
              icon: Icons.receipt_long_outlined,
              title: 'Không có booking',
              subtitle: 'Không tìm thấy hợp đồng liên quan.',
            );
          }

          final booking = bookings.first;

          return Padding(
            padding: const EdgeInsets.all(AppTheme.pagePadding),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Booking ${booking.id}', style: AppTheme.heading2),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    TextButton.icon(
                      onPressed: () => _showAddChildDialog(context, groupVm, booking.id),
                      icon: const Icon(Icons.add, size: 18),
                      label: const Text('Thêm tài khoản con'),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Expanded(
                  child: Builder(builder: (ctx) {
                    final cState = groupVm.childrenState;
                    if (cState is Loading) return const Center(child: CircularProgressIndicator());
                    if (cState is Failure) {
                      return AppEmptyState(
                        icon: Icons.error_outline,
                        title: 'Lỗi tải tài khoản con',
                        subtitle: (cState as Failure).message,
                      );
                    }
                    final children = switch (cState) {
                      Success<List<ChildAccount>> s => s.data,
                      _ => <ChildAccount>[],
                    };
                    if (children.isEmpty) {
                      return AppEmptyState(
                        icon: Icons.group_add_outlined,
                        title: 'Chưa có tài khoản con',
                        subtitle: 'Nhấn Thêm để tạo.',
                      );
                    }

                    return ListView.separated(
                      itemCount: children.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 12),
                      itemBuilder: (context, index) {
                        final child = children[index];
                        return AccountMemberCard(
                          fullName: child.fullName,
                          phone: child.phone,
                          trailing: IconButton(
                            icon: const Icon(Icons.delete_outline, color: Colors.red),
                            onPressed: () => _showDeleteConfirmDialog(context, groupVm, child, booking.id),
                          ),
                        );
                      },
                    );
                  }),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Future<void> _showAddChildDialog(BuildContext context, GroupAccountsViewModel groupVm, String bookingId) async {
    final nameCtl = TextEditingController();
    final phoneCtl = TextEditingController();

    final ok = await showDialog<bool>(
      context: context,
      builder: (dCtx) => AlertDialog(
        title: const Text('Thêm tài khoản con'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(controller: nameCtl, decoration: const InputDecoration(labelText: 'Họ và tên')),
              const SizedBox(height: 8),
              TextField(controller: phoneCtl, decoration: const InputDecoration(labelText: 'Số điện thoại')),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dCtx).pop(false),
            child: const Text('Hủy'),
          ),
          FilledButton(
            onPressed: () async {
              final name = nameCtl.text.trim();
              final phone = phoneCtl.text.trim();
              if (name.isEmpty || phone.isEmpty) return;
              await groupVm.addChild(bookingId: bookingId, fullName: name, phone: phone);
              Navigator.of(dCtx).pop(true);
            },
            child: const Text('Thêm'),
          ),
        ],
      ),
    );
    if (ok == true) {
      await groupVm.fetchChildren(bookingId);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Đã thêm tài khoản con')));
      }
    }
  }

  Future<void> _showDeleteConfirmDialog(BuildContext context, GroupAccountsViewModel groupVm, ChildAccount child, String bookingId) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (d) => AlertDialog(
        title: const Text('Xác nhận'),
        content: Text('Xóa tài khoản con "${child.fullName}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(d).pop(false),
            child: const Text('Hủy'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(d).pop(true),
            style: FilledButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('Xóa'),
          ),
        ],
      ),
    );
    if (ok == true) {
      await groupVm.removeChild(child.id);
      await groupVm.fetchChildren(bookingId);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Đã xóa')));
      }
    }
  }
}
