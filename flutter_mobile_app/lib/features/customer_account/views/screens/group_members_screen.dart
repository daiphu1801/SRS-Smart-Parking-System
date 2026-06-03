import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_account/models/group_member.dart';
import 'package:smart_parking_mobile/features/customer_account/viewmodels/group_members_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_account/views/widgets/customer_widgets.dart';

class GroupMembersScreen extends StatefulWidget {
  const GroupMembersScreen({super.key});

  @override
  State<GroupMembersScreen> createState() => _GroupMembersScreenState();
}

class _GroupMembersScreenState extends State<GroupMembersScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<GroupMembersViewModel>().fetchMembers();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Quản lý thành viên trong nhóm')),
      body: Consumer<GroupMembersViewModel>(
        builder: (context, groupVm, child) {
          return Padding(
            padding: const EdgeInsets.all(AppTheme.pagePadding),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Danh sách thành viên', style: AppTheme.heading2),
                    TextButton.icon(
                      onPressed: () => _showAddMemberDialog(context, groupVm),
                      icon: const Icon(Icons.person_add_alt_1, size: 18),
                      label: const Text('Thêm mới'),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Expanded(
                  child: Builder(
                    builder: (ctx) {
                      final mState = groupVm.membersState;
                      
                      if (mState is Loading) {
                        return const Center(child: CircularProgressIndicator());
                      }
                      
                      if (mState is Failure) {
                        return AppEmptyState(
                          icon: Icons.error_outline,
                          title: 'Lỗi tải danh sách',
                          subtitle: (mState as Failure).message,
                        );
                      }
                      
                      final members = switch (mState) {
                        Success<List<GroupMember>> s => s.data,
                        _ => <GroupMember>[],
                      };
                      
                      if (members.isEmpty) {
                        return AppEmptyState(
                          icon: Icons.group_add_outlined,
                          title: 'Chưa có thành viên nào',
                          subtitle: 'Nhấn Thêm mới để thêm thành viên vào nhóm.',
                        );
                      }

                      return ListView.separated(
                        itemCount: members.length,
                        separatorBuilder: (_, _) => const SizedBox(height: 12),
                        itemBuilder: (context, index) {
                          final member = members[index];
                          return AccountMemberCard(
                            fullName: member.fullName,
                            phone: member.phone,
                            trailing: IconButton(
                              icon: const Icon(
                                Icons.delete_outline,
                                color: Colors.red,
                              ),
                              onPressed: () => _showDeleteConfirmDialog(
                                context,
                                groupVm,
                                member,
                              ),
                            ),
                          );
                        },
                      );
                    },
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Future<void> _showAddMemberDialog(
    BuildContext context,
    GroupMembersViewModel groupVm,
  ) async {
    final nameCtl = TextEditingController();
    final phoneCtl = TextEditingController();

    final ok = await showDialog<bool>(
      context: context,
      builder: (dCtx) => AlertDialog(
        title: const Text('Thêm thành viên'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: nameCtl,
                decoration: const InputDecoration(labelText: 'Họ và tên'),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: phoneCtl,
                decoration: const InputDecoration(labelText: 'Số điện thoại'),
                keyboardType: TextInputType.phone,
              ),
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
              
              try {
                await groupVm.addMember(
                  fullName: name,
                  phone: phone,
                );
                if (!dCtx.mounted) return;
                Navigator.of(dCtx).pop(true);
              } catch (e) {
                if (!dCtx.mounted) return;
                ScaffoldMessenger.of(dCtx).showSnackBar(
                  SnackBar(content: Text('Lỗi: \${e.toString()}')),
                );
              }
            },
            child: const Text('Thêm'),
          ),
        ],
      ),
    );
    
    if (ok == true && context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đã thêm thành viên mới')),
      );
    }
  }

  Future<void> _showDeleteConfirmDialog(
    BuildContext context,
    GroupMembersViewModel groupVm,
    GroupMember member,
  ) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (d) => AlertDialog(
        title: const Text('Xác nhận'),
        content: Text('Xóa thành viên "\${member.fullName}" khỏi nhóm?'),
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
      try {
        await groupVm.removeMember(member.id);
        if (!context.mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Đã xóa thành viên')),
        );
      } catch (e) {
        if (!context.mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Lỗi khi xóa: \${e.toString()}')),
        );
      }
    }
  }
}
