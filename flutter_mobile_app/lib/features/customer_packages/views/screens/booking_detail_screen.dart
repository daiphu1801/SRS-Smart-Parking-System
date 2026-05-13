import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/features/customer_account/viewmodels/group_accounts_viewmodel.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/widgets/package_widgets.dart';
import 'package:intl/intl.dart';

class BookingDetailScreen extends StatefulWidget {
  final String bookingId;
  const BookingDetailScreen({super.key, required this.bookingId});

  @override
  State<BookingDetailScreen> createState() => _BookingDetailScreenState();
}

class _BookingDetailScreenState extends State<BookingDetailScreen> {
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: context.canPop()
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => context.pop(),
              )
            : null,
        title: const Text('Chi tiết gói cước tháng'),
      ),
      body: ChangeNotifierProvider<GroupAccountsViewModel>(
        create: (_) => GroupAccountsViewModel(),
        child: Consumer2<BookingViewModel, GroupAccountsViewModel>(
          builder: (context, vm, groupVm, child) {
            return switch (vm.currentBookingState) {
              Idle() || Loading() => const Center(child: CircularProgressIndicator()),
              Failure(message: var msg) => AppEmptyState(
                  icon: Icons.error_outline,
                  title: 'Lỗi tải dữ liệu',
                  subtitle: msg,
                ),
              Success(data: var booking) => Builder(builder: (context) {
                  // ensure children are loaded for this booking
                  if (groupVm.childrenState is Idle) {
                    groupVm.fetchChildren(booking.id);
                  }

                  final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');

                  return SingleChildScrollView(
                    padding: const EdgeInsets.all(AppTheme.pagePadding),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // ── Tổng quan booking ────────────────────────────────────
                        AppCard(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text('Hợp đồng ${booking.id}', style: AppTheme.heading3),
                                      const SizedBox(height: 4),
                                      Text('Quản lý phương tiện',
                                          style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                                    ],
                                  ),
                                  AppBadge(
                                    label: booking.paymentStatus.label,
                                    isFilled: booking.paymentStatus == PaymentStatus.success,
                                  ),
                                ],
                              ),
                              const Divider(height: 24),
                              _buildInfoRow(
                                'Nhóm / đại diện:',
                                booking.groupName,
                              ),
                              const SizedBox(height: 8),
                              _buildInfoRow(
                                'Ngày tạo:',
                                dateFormatter.format(booking.createdAt),
                              ),
                              const SizedBox(height: 8),
                              _buildInfoRow(
                                'Số xe đăng ký:',
                                '${booking.totalVehicles} xe',
                              ),
                              const SizedBox(height: 8),
                              if (booking.paymentMethod != null) ...[
                                _buildInfoRow(
                                  'Phương thức thanh toán:',
                                  booking.paymentMethod!.label,
                                ),
                                const SizedBox(height: 8),
                              ],
                              const SizedBox(height: 12),
                              SizedBox(
                                width: double.infinity,
                                child: OutlinedButton.icon(
                                  onPressed: () => context.push('/customer/bookings/${booking.id}/add-vehicle'),
                                  icon: const Icon(Icons.add_circle_outline),
                                  label: const Text('Thêm xe vào booking'),
                                ),
                              ),
                            ],
                          ),
                        ),

                        const SizedBox(height: AppTheme.sectionGap),

                        // ── Danh sách booking detail ─────────────────────────────
                        Text('Danh sách Booking Detail', style: AppTheme.heading3),
                        const SizedBox(height: 12),
                        ListView.separated(
                          shrinkWrap: true,
                          physics: const NeverScrollableScrollPhysics(),
                          itemCount: booking.details.length,
                          separatorBuilder: (context, index) => const SizedBox(height: 12),
                          itemBuilder: (context, index) {
                            return BookingDetailCard(detail: booking.details[index]);
                          },
                        ),

                        const SizedBox(height: AppTheme.sectionGap),

                        // ── Child Accounts Section ──────────────────────────────
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text('Tài khoản con', style: AppTheme.heading3),
                            TextButton.icon(
                              onPressed: () async {
                                final nameCtl = TextEditingController();
                                final addrCtl = TextEditingController();
                                final phoneCtl = TextEditingController();

                                final ok = await showDialog<bool>(
                                  context: context,
                                  builder: (dCtx) => AlertDialog(
                                    title: const Text('Thêm tài khoản con'),
                                    content: SingleChildScrollView(
                                      child: Column(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          TextField(
                                              controller: nameCtl, decoration: const InputDecoration(labelText: 'Họ và tên')),
                                          const SizedBox(height: 8),
                                          TextField(
                                              controller: addrCtl, decoration: const InputDecoration(labelText: 'Địa chỉ')),
                                          const SizedBox(height: 8),
                                          TextField(
                                              controller: phoneCtl,
                                              decoration: const InputDecoration(labelText: 'Số điện thoại')),
                                        ],
                                      ),
                                    ),
                                    actions: [
                                      TextButton(onPressed: () => Navigator.of(dCtx).pop(false), child: const Text('Hủy')),
                                      AppFilledButton(
                                        label: 'Thêm',
                                        onPressed: () async {
                                          final name = nameCtl.text.trim();
                                          final address = addrCtl.text.trim();
                                          final phone = phoneCtl.text.trim();
                                          if (name.isEmpty || phone.isEmpty) {
                                            ScaffoldMessenger.of(context).showSnackBar(
                                                const SnackBar(content: Text('Họ tên và số điện thoại là bắt buộc')));
                                            return;
                                          }

                                          await groupVm.addChild(
                                              bookingId: booking.id, fullName: name, address: address, phone: phone);
                                          if (!dCtx.mounted) return;
                                          Navigator.of(dCtx).pop(true);
                                          await vm.fetchBookingById(booking.id);
                                          await groupVm.fetchChildren(booking.id);
                                        },
                                      ),
                                    ],
                                  ),
                                );
                                if (ok == true && mounted) {
                                  ScaffoldMessenger.of(context)
                                      .showSnackBar(const SnackBar(content: Text('Đã thêm tài khoản con')));
                                }
                              },
                              icon: const Icon(Icons.add, size: 18),
                              label: const Text('Thêm tài khoản con'),
                            ),
                          ],
                        ),

                        const SizedBox(height: 12),
                        Builder(builder: (ctx) {
                          return switch (groupVm.childrenState) {
                            Idle() || Loading() => const Center(child: CircularProgressIndicator()),
                            Failure(message: var msg) => AppEmptyState(
                                icon: Icons.error_outline,
                                title: 'Lỗi tải tài khoản con',
                                subtitle: msg,
                              ),
                            Success(data: var children) when children.isEmpty => const AppEmptyState(
                                icon: Icons.group_add_outlined,
                                title: 'Chưa có tài khoản con',
                                subtitle: 'Bạn có thể thêm tài khoản con để quản lý phương tiện riêng.',
                              ),
                            Success(data: var children) => ListView.separated(
                                shrinkWrap: true,
                                physics: const NeverScrollableScrollPhysics(),
                                itemCount: children.length,
                                separatorBuilder: (context, index) => const SizedBox(height: 12),
                                itemBuilder: (context, index) {
                                  final child = children[index];
                                  final vehicleCount = booking.details.where((d) => d.customerId == child.id).length;
                                  return AppCard(
                                    padding: const EdgeInsets.all(12),
                                    child: Row(
                                      children: [
                                        Container(
                                          width: 48,
                                          height: 48,
                                          decoration: BoxDecoration(
                                              color: AppTheme.primary.withValues(alpha: 0.1), shape: BoxShape.circle),
                                          alignment: Alignment.center,
                                          child: Text(child.fullName.isNotEmpty ? child.fullName[0] : '-',
                                              style: AppTheme.heading3.copyWith(color: AppTheme.primary)),
                                        ),
                                        const SizedBox(width: 12),
                                        Expanded(
                                          child: Column(
                                            crossAxisAlignment: CrossAxisAlignment.start,
                                            children: [
                                              Text(child.fullName, style: AppTheme.heading3),
                                              const SizedBox(height: 4),
                                              Text(_maskPhone(child.phone),
                                                  style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                                            ],
                                          ),
                                        ),
                                        Column(
                                          crossAxisAlignment: CrossAxisAlignment.end,
                                          children: [
                                            AppBadge(label: '$vehicleCount xe'),
                                            const SizedBox(height: 8),
                                            TextButton(
                                              onPressed: () => context.push('/customer/bookings/${booking.id}/add-vehicle',
                                                  extra: child.id),
                                              child: const Text('Thêm xe'),
                                            ),
                                          ],
                                        ),
                                      ],
                                    ),
                                  );
                                },
                              ),
                            _ => const SizedBox.shrink(),
                          };
                        }),
                      ],
                    ),
                  );
                }),
              _ => const SizedBox.shrink(),
            };
          },
        ),
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
            style: isTotal ? AppTheme.heading3.copyWith(fontSize: 15, color: AppTheme.primary) : AppTheme.body,
          ),
        ),
      ],
    );
  }
}
