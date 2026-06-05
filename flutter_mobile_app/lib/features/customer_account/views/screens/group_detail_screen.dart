import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_account/viewmodels/customer_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_account/models/customer_models.dart';
import 'package:smart_parking_mobile/features/customer_account/views/widgets/customer_widgets.dart';

class GroupDetailScreen extends StatefulWidget {
  final String customerId;
  const GroupDetailScreen({super.key, required this.customerId});

  @override
  State<GroupDetailScreen> createState() => _GroupDetailScreenState();
}

class _GroupDetailScreenState extends State<GroupDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<CustomerViewModel>().fetchGroupCustomer(widget.customerId);
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    return Scaffold(
      appBar: AppBar(
        title: Text('Nhóm khách hàng', style: AppTheme.heading1),
      ),
      body: Consumer<CustomerViewModel>(
        builder: (context, viewModel, child) {
          final state = viewModel.groupState;

          if (state is Loading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is Failure) {
            return AppEmptyState(
              icon: Icons.group_off,
              title: 'Không thể tải thông tin nhóm',
              subtitle: (state as Failure).message,
            );
          }

          if (state is Success<GroupCustomer>) {
            final group = state.data;
            final isOwner = group.ownerId == widget.customerId;

            return CustomScrollView(
              slivers: [
                SliverPadding(
                  padding: const EdgeInsets.all(AppTheme.pagePadding),
                  sliver: SliverList(
                    delegate: SliverChildListDelegate([
                      AppCard(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            InfoRow(label: 'Tên nhóm', value: group.name),
                            Divider(height: 24, color: AppTheme.border),
                            InfoRow(label: 'Trưởng nhóm', value: group.ownerName),
                            Divider(height: 24, color: AppTheme.border),
                            InfoRow(label: 'Tổng số thành viên', value: '${group.customers.length} thành viên'),
                          ],
                        ),
                      ),
                      const SizedBox(height: 24),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(l10n.memberList, style: AppTheme.heading2),
                          if (isOwner)
                            TextButton.icon(
                              onPressed: () => context.push('/customer/group/edit/${group.id}'),
                              icon: const Icon(Icons.edit, size: 18),
                              label: const Text('Quản lý'),
                            ),
                        ],
                      ),
                      const SizedBox(height: 12),
                    ]),
                  ),
                ),
                SliverPadding(
                  padding: const EdgeInsets.symmetric(horizontal: AppTheme.pagePadding),
                  sliver: SliverList(
                    delegate: SliverChildBuilderDelegate(
                      (context, index) {
                        final member = group.customers[index];
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 12.0),
                          child: AccountMemberCard(
                            fullName: member.fullName,
                            phone: member.phone,
                            badgeLabel: member.id == group.ownerId ? 'Trưởng nhóm' : null,
                            isBadgeFilled: true,
                          ),
                        );
                      },
                      childCount: group.customers.length,
                    ),
                  ),
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
