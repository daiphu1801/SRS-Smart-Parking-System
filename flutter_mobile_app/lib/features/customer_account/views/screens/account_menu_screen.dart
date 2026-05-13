import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/features/customer_account/viewmodels/customer_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_account/models/customer_models.dart';
import 'package:smart_parking_mobile/features/customer_account/views/widgets/customer_widgets.dart';

class AccountMenuScreen extends StatefulWidget {
  const AccountMenuScreen({super.key});

  @override
  State<AccountMenuScreen> createState() => _AccountMenuScreenState();
}

class _AccountMenuScreenState extends State<AccountMenuScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<CustomerViewModel>().fetchCustomer('CUST-001');
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Tài khoản', style: AppTheme.heading1),
      ),
      body: Consumer<CustomerViewModel>(
        builder: (context, vm, child) {
          final state = vm.customerState;

          final headerWidget = switch (state) {
            Loading() => const Center(
                child: Padding(
                  padding: EdgeInsets.all(32.0),
                  child: CircularProgressIndicator(),
                ),
              ),
            Success<Customer>(data: final customer) => _buildProfileHeader(context, customer),
            Failure<Customer>(message: final msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: 'Lỗi tải hồ sơ',
                subtitle: msg,
              ),
            _ => const SizedBox.shrink(),
          };

          return ListView(
            padding: const EdgeInsets.all(AppTheme.pagePadding),
            children: [
              headerWidget,
              const SizedBox(height: 32),
              Text('Cài đặt & Hỗ trợ', style: AppTheme.heading3),
              const SizedBox(height: 16),
              MenuCard(
                icon: Icons.person_outline,
                title: 'Chi tiết hồ sơ',
                subtitle: 'Xem thông tin đầy đủ, CCCD, địa chỉ',
                onTap: () => context.push('/customer/detail/CUST-001'),
              ),
              const SizedBox(height: 12),
              MenuCard(
                icon: Icons.group_outlined,
                title: 'Tài khoản con',
                subtitle: 'Quản lý tài khoản con và phương tiện',
                onTap: () => context.push('/customer/child-accounts'),
              ),
              const SizedBox(height: 12),
              MenuCard(
                icon: Icons.lock_outline,
                title: 'Đổi mật khẩu',
                subtitle: 'Cập nhật mật khẩu bảo mật',
                onTap: () => context.push(AppRoutes.changePassword),
              ),
              const SizedBox(height: 12),
              MenuCard(
                icon: Icons.feedback_outlined,
                title: 'Hỗ trợ & Khiếu nại',
                subtitle: 'Gửi ý kiến đóng góp',
                onTap: () => context.push(AppRoutes.complaintList),
              ),
              const SizedBox(height: 12),
              MenuCard(
                icon: Icons.notifications_none_outlined,
                title: 'Thông báo',
                subtitle: 'Quản lý thông báo',
                onTap: () => context.push(AppRoutes.notificationList),
              ),
              const SizedBox(height: 32),
              OutlinedButton.icon(
                onPressed: () {
                  context.read<AuthViewModel>().logout();
                },
                icon: const Icon(Icons.logout, color: Colors.red),
                label: const Text('Đăng xuất'),
                style: OutlinedButton.styleFrom(
                  foregroundColor: Colors.red,
                  side: const BorderSide(color: Colors.red),
                  padding: const EdgeInsets.symmetric(vertical: 14),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildProfileHeader(BuildContext context, Customer customer) {
    return AppCard(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                width: 64,
                height: 64,
                decoration: BoxDecoration(
                  color: AppTheme.primary.withValues(alpha: 0.1),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.person, size: 36, color: AppTheme.primary),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(customer.fullName, style: AppTheme.heading2),
                    const SizedBox(height: 4),
                    Text(
                      customer.email,
                      style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                    ),
                  ],
                ),
              ),
              IconButton(
                icon: const Icon(Icons.edit_outlined, color: AppTheme.primary),
                tooltip: 'Sửa hồ sơ',
                onPressed: () => context.push('/customer/edit/${customer.id}'),
              ),
            ],
          ),
          const Divider(height: 32),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              QuickInfoItem(icon: Icons.phone_outlined, label: 'Điện thoại', value: customer.phone),
              QuickInfoItem(icon: Icons.group_outlined, label: 'Nhóm', value: customer.groupName),
            ],
          ),
        ],
      ),
    );
  }
}
