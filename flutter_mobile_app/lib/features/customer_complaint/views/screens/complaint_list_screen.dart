import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_complaint/viewmodels/complaint_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_complaint/views/widgets/complaint_widgets.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

class ComplaintListScreen extends StatefulWidget {
  const ComplaintListScreen({super.key});

  @override
  State<ComplaintListScreen> createState() => _ComplaintListScreenState();
}

class _ComplaintListScreenState extends State<ComplaintListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      // Mock customer ID
      context.read<ComplaintViewModel>().fetchComplaints();
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        leading: context.canPop()
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => context.pop(),
              )
            : null,
        title: Text(l10n.complaintsFeedback),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            tooltip: l10n.filter,
            onPressed: () {},
          ),
        ],
      ),
      body: Consumer<ComplaintViewModel>(
        builder: (context, vm, _) {
          return switch (vm.complaintsState) {
            Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: l10n.dataLoadError,
                subtitle: msg,
              ),
            Success(data: var complaints) when complaints.isEmpty => AppEmptyState(
                icon: Icons.speaker_notes_off_outlined,
                title: l10n.noComplaintsYet,
                subtitle: l10n.complaintsFeedbackEncourage,
              ),
            Success(data: var complaints) => RefreshIndicator(
                color: AppTheme.primary,
                onRefresh: () => vm.fetchComplaints(),
                child: ListView.separated(
                  padding: const EdgeInsets.only(
                    left: AppTheme.pagePadding,
                    right: AppTheme.pagePadding,
                    top: AppTheme.pagePadding,
                    bottom: 80, // Space for FAB
                  ),
                  itemCount: complaints.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 12),
                  itemBuilder: (context, i) => ComplaintCard(complaint: complaints[i]),
                ),
              ),
            _ => const SizedBox.shrink(),
          };
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/customer/complaints/create'),
        icon: const Icon(Icons.add_comment_outlined, color: Colors.white),
        label: Text(l10n.createComplaint, style: const TextStyle(color: Colors.white)),
        backgroundColor: AppTheme.primary,
      ),
    );
  }
}
