import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
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
    final l10n = AppLocalizations.of(context)!;
    return Scaffold(
      appBar: AppBar(title: Text(l10n.manageGroupMembers)),
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
                    Text(l10n.memberList, style: AppTheme.heading2),
                    TextButton.icon(
                      onPressed: () => _showAddMemberDialog(context, groupVm, l10n),
                      icon: const Icon(Icons.person_add_alt_1, size: 18),
                      label: Text(l10n.addMember),
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
                          title: l10n.noMembers,
                          subtitle: l10n.addMemberPrompt,
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
                                l10n,
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
    AppLocalizations l10n,
  ) async {
    final nameCtl = TextEditingController();
    final phoneCtl = TextEditingController();

    final ok = await showDialog<bool>(
      context: context,
      builder: (dCtx) => AlertDialog(
        title: Text(l10n.addMember),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: nameCtl,
                decoration: InputDecoration(labelText: l10n.fullName),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: phoneCtl,
                decoration: InputDecoration(labelText: l10n.phoneNumber),
                keyboardType: TextInputType.phone,
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dCtx).pop(false),
            child: Text(l10n.cancel),
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
                  SnackBar(content: Text(e.toString())),
                );
              }
            },
            child: Text(l10n.add),
          ),
        ],
      ),
    );
    
    if (ok == true && context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(l10n.addedNewMember)),
      );
    }
  }

  Future<void> _showDeleteConfirmDialog(
    BuildContext context,
    GroupMembersViewModel groupVm,
    GroupMember member,
    AppLocalizations l10n,
  ) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (d) => AlertDialog(
        title: Text(l10n.confirm),
        content: Text(l10n.removeMemberConfirm(member.fullName)),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(d).pop(false),
            child: Text(l10n.cancel),
          ),
          FilledButton(
            onPressed: () => Navigator.of(d).pop(true),
            style: FilledButton.styleFrom(backgroundColor: Colors.red),
            child: Text(l10n.delete),
          ),
        ],
      ),
    );
    
    if (ok == true) {
      try {
        await groupVm.removeMember(member.id);
        if (!context.mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(l10n.removedMember)),
        );
      } catch (e) {
        if (!context.mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(l10n.deleteFailed(e.toString()))),
        );
      }
    }
  }
}
