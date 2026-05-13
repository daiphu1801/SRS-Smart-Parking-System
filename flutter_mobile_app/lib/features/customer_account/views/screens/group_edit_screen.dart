import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_account/viewmodels/customer_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_account/models/customer_models.dart';
import 'package:smart_parking_mobile/features/customer_account/views/widgets/customer_widgets.dart';

class GroupEditScreen extends StatefulWidget {
  final String groupId;
  const GroupEditScreen({super.key, required this.groupId});

  @override
  State<GroupEditScreen> createState() => _GroupEditScreenState();
}

class _GroupEditScreenState extends State<GroupEditScreen> {
  bool _isLoading = false;
  List<Customer> _editableList = [];

  @override
  void initState() {
    super.initState();
    final state = context.read<CustomerViewModel>().groupState;
    if (state is Success<GroupCustomer>) {
      _editableList = List.from(state.data.customers);
    }
  }

  void _removeMember(int index) {
    setState(() {
      _editableList.removeAt(index);
    });
  }

  Future<void> _saveChanges() async {
    setState(() => _isLoading = true);
    
    final viewModel = context.read<CustomerViewModel>();
    final success = await viewModel.updateGroupCustomer(widget.groupId, _editableList);
    
    setState(() => _isLoading = false);
    
    if (success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cập nhật danh sách thành viên thành công!')),
      );
      context.pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = context.read<CustomerViewModel>().groupState;
    if (state is! Success<GroupCustomer>) {
      return Scaffold(
        appBar: AppBar(title: Text('Quản lý nhóm', style: AppTheme.heading1)),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    final group = state.data;

    return Scaffold(
      appBar: AppBar(
        title: Text('Quản lý thành viên', style: AppTheme.heading1),
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.separated(
              padding: const EdgeInsets.all(AppTheme.pagePadding),
              itemCount: _editableList.length,
              separatorBuilder: (context, index) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                final member = _editableList[index];
                final isOwner = member.id == group.ownerId;
                
                return AccountMemberCard(
                  fullName: member.fullName,
                  phone: member.phone,
                  badgeLabel: isOwner ? 'Trưởng nhóm' : null,
                  isBadgeFilled: true,
                  trailing: isOwner
                      ? null
                      : IconButton(
                          icon: const Icon(Icons.remove_circle_outline, color: Colors.red),
                          onPressed: () => _removeMember(index),
                        ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(AppTheme.pagePadding),
            child: AppFilledButton(
              label: 'Lưu thay đổi',
              onPressed: _saveChanges,
              isLoading: _isLoading,
            ),
          )
        ],
      ),
    );
  }
}
