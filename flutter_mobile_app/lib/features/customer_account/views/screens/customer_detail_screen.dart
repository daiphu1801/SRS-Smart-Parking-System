import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_profile/viewmodels/profile_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_profile/models/customer_profile_models.dart';
import 'package:smart_parking_mobile/features/customer_account/views/widgets/customer_widgets.dart';

class CustomerDetailScreen extends StatefulWidget {
  final String customerId;
  const CustomerDetailScreen({super.key, required this.customerId});

  @override
  State<CustomerDetailScreen> createState() => _CustomerDetailScreenState();
}

class _CustomerDetailScreenState extends State<CustomerDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ProfileViewModel>().fetchProfile();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Hồ sơ khách hàng', style: AppTheme.heading1),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit, color: AppTheme.primary),
            onPressed: () {
              final state = context.read<ProfileViewModel>().profileState;
              if (state is Success) {
                context.push('/customer/edit/${widget.customerId}');
              }
            },
          )
        ],
      ),
      body: Consumer<ProfileViewModel>(
        builder: (context, viewModel, child) {
          final state = viewModel.profileState;

          if (state is Loading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is Failure) {
            return AppEmptyState(
              icon: Icons.error_outline,
              title: 'Không thể tải thông tin khách hàng',
              subtitle: (state as Failure).message,
            );
          }

          if (state is Success<CustomerProfile>) {
            final customer = state.data;
            return SingleChildScrollView(
              padding: const EdgeInsets.all(AppTheme.pagePadding),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  AppCard(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        InfoRow(label: 'Họ và tên', value: customer.fullName),
                        Divider(height: 24, color: AppTheme.border),
                        InfoRow(label: 'Số điện thoại', value: customer.phone),
                        Divider(height: 24, color: AppTheme.border),
                        InfoRow(label: 'Địa chỉ', value: customer.address),
                        if (customer.groupName != null) ...[
                          Divider(height: 24, color: AppTheme.border),
                          InfoRow(
                            label: 'Nhóm khách hàng',
                            value: customer.groupName!,
                            trailing: GestureDetector(
                              onTap: () => context.push('/customer/group/${widget.customerId}'),
                              child: Text(
                                customer.groupName!,
                                textAlign: TextAlign.right,
                                style: AppTheme.label.copyWith(
                                  color: AppTheme.primary,
                                  decoration: TextDecoration.underline,
                                ),
                              ),
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                  const SizedBox(height: AppTheme.sectionGap),
                  OutlinedButton.icon(
                    onPressed: () => context.push('/customer/change-password'),
                    icon: const Icon(Icons.lock_outline_rounded),
                    label: const Text('Đổi mật khẩu'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppTheme.primary,
                      side: const BorderSide(color: AppTheme.primary),
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                  ),
                ],
              ),
            );
          }

          return const SizedBox.shrink();
        },
      ),
    );
  }
}
