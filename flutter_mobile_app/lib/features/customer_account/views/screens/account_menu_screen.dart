import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/features/customer_profile/viewmodels/profile_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_profile/models/customer_profile_models.dart';
import 'package:smart_parking_mobile/features/customer_account/views/widgets/customer_widgets.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

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
      context.read<ProfileViewModel>().fetchProfile();
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.accountTitle, style: AppTheme.heading1),
      ),
      body: Consumer<ProfileViewModel>(
        builder: (context, vm, child) {
          final state = vm.profileState;

          final headerWidget = switch (state) {
            Loading() => const Center(
                child: Padding(
                  padding: EdgeInsets.all(32.0),
                  child: CircularProgressIndicator(),
                ),
              ),
            Success<CustomerProfile>(data: final customer) => _buildProfileHeader(context, customer),
            Failure<CustomerProfile>(message: final msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: l10n.errorLoadingProfile,
                subtitle: msg,
              ),
            _ => const SizedBox.shrink(),
          };

          return ListView(
            padding: const EdgeInsets.all(AppTheme.pagePadding),
            children: [
              headerWidget,
              const SizedBox(height: 32),
              Text(l10n.settingsAndSupport, style: AppTheme.heading3),
              const SizedBox(height: 16),
              MenuCard(
                icon: Icons.person_outline,
                title: l10n.profileDetails,
                subtitle: l10n.profileDetailsSubtitle,
                onTap: () => context.push('/customer/detail/CUST-001'),
              ),
              if (state is Success<CustomerProfile> && state.data.groupId != null) ...[
                const SizedBox(height: 12),
                MenuCard(
                  icon: Icons.group_outlined,
                  title: l10n.manageGroupMembers,
                  subtitle: l10n.manageGroupMembersSubtitle,
                  onTap: () => context.push('/customer/group-members'),
                ),
              ],
              const SizedBox(height: 12),
              MenuCard(
                icon: Icons.lock_outline,
                title: l10n.changePassword,
                subtitle: l10n.changePasswordSubtitle,
                onTap: () => context.push(AppRoutes.changePassword),
              ),
              const SizedBox(height: 12),
              MenuCard(
                icon: Icons.feedback_outlined,
                title: l10n.supportAndComplaints,
                subtitle: l10n.supportAndComplaintsSubtitle,
                onTap: () => context.push(AppRoutes.complaintCreate),
              ),
              const SizedBox(height: 12),
              MenuCard(
                icon: Icons.notifications_none_outlined,
                title: l10n.notifications,
                subtitle: l10n.notificationsSubtitle,
                onTap: () => context.push(AppRoutes.notificationList),
              ),
              const SizedBox(height: 32),
              OutlinedButton.icon(
                onPressed: () {
                  context.read<AuthViewModel>().logout();
                },
                icon: const Icon(Icons.logout, color: Colors.red),
                label: Text(l10n.logout),
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

  Widget _buildProfileHeader(BuildContext context, CustomerProfile customer) {
    final l10n = AppLocalizations.of(context)!;

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
                      customer.phone,
                      style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                    ),
                  ],
                ),
              ),
              IconButton(
                icon: const Icon(Icons.edit_outlined, color: AppTheme.primary),
                tooltip: l10n.editProfileTooltip,
                onPressed: () => context.push('/customer/edit/${customer.id}'),
              ),
            ],
          ),
          const Divider(height: 32),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              QuickInfoItem(icon: Icons.phone_outlined, label: l10n.phoneLabel, value: customer.phone),
              if (customer.groupName != null)
                QuickInfoItem(icon: Icons.group_outlined, label: l10n.groupLabel, value: customer.groupName!),
            ],
          ),
        ],
      ),
    );
  }
}
