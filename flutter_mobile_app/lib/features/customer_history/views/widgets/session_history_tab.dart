import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/features/customer_history/viewmodels/history_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_history/views/widgets/session_filter_bottom_sheet.dart';
import 'package:smart_parking_mobile/features/customer_history/views/widgets/session_card.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

class SessionHistoryTab extends StatelessWidget {
  const SessionHistoryTab({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<HistoryViewModel>(
      builder: (context, vm, _) {
        return Column(
          children: [
            _FilterBar(vm: vm),
            if (vm.hasActiveFilter) _ActiveFilterBadge(vm: vm),
            Expanded(child: _SessionHistoryList(vm: vm)),
          ],
        );
      },
    );
  }
}

class _FilterBar extends StatelessWidget {
  final HistoryViewModel vm;
  const _FilterBar({required this.vm});

  Future<void> _openFilterSheet(BuildContext context) async {
    final result = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppTheme.background,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => FilterBottomSheet(
        initialStartDate: vm.startDate,
        initialEndDate: vm.endDate,
        initialPlateNumber: vm.plateNumber,
      ),
    );

    if (result != null && context.mounted) {
      await vm.applyFilter(
        newStartDate: result['startDate'],
        newEndDate: result['endDate'],
        newPlateNumber: result['plateNumber'],
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: AppTheme.pagePadding,
        vertical: 8,
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(AppLocalizations.of(context)!.parkingHistoryTitle, style: AppTheme.heading3),
          TextButton.icon(
            onPressed: () => _openFilterSheet(context),
            icon: const Icon(Icons.filter_list, size: 20),
            label: Text(AppLocalizations.of(context)!.filterButtonText),
            style: TextButton.styleFrom(
              foregroundColor: AppTheme.primary,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              backgroundColor: AppTheme.primary.withValues(alpha: 0.1),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ActiveFilterBadge extends StatelessWidget {
  final HistoryViewModel vm;
  const _ActiveFilterBadge({required this.vm});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: AppTheme.pagePadding,
      ).copyWith(bottom: 8),
      child: Row(
        children: [
          Icon(Icons.check_circle, size: 16, color: AppTheme.primary),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              AppLocalizations.of(context)!.filterAppliedText,
              style: AppTheme.bodySmall.copyWith(
                color: AppTheme.primary,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          InkWell(
            onTap: () => vm.clearFilter(),
            child: Text(
              AppLocalizations.of(context)!.clearFilterButton,
              style: AppTheme.bodySmall.copyWith(color: Colors.red),
            ),
          ),
        ],
      ),
    );
  }
}

class _SessionHistoryList extends StatelessWidget {
  final HistoryViewModel vm;
  const _SessionHistoryList({required this.vm});

  @override
  Widget build(BuildContext context) {
    final state = vm.historyState;

    if (state is Loading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (state is Failure) {
      return AppEmptyState(
        icon: Icons.error_outline,
        title: AppLocalizations.of(context)!.failedToLoadData,
        subtitle: (state as Failure).message,
      );
    }

    if (state is Success<List<ParkingSession>>) {
      final sessions = state.data;

      if (sessions.isEmpty) {
        return AppEmptyState(
          icon: Icons.history,
          title: AppLocalizations.of(context)!.noParkingHistoryFound,
          subtitle: AppLocalizations.of(context)!.changeFilterSubtitle,
        );
      }

      return RefreshIndicator(
        color: AppTheme.primary,
        onRefresh: () => vm.fetchHistory(),
        child: ListView.separated(
          padding: const EdgeInsets.all(AppTheme.pagePadding),
          itemCount: sessions.length,
          separatorBuilder: (context, index) => const SizedBox(height: 12),
          itemBuilder: (context, i) => SessionCard(session: sessions[i]),
        ),
      );
    }

    return const SizedBox.shrink();
  }
}
