import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_parking/viewmodels/parking_session_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_parking/views/widgets/parking_widgets.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

class ParkingSessionListScreen extends StatefulWidget {
  const ParkingSessionListScreen({super.key});

  @override
  State<ParkingSessionListScreen> createState() =>
      _ParkingSessionListScreenState();
}

class _ParkingSessionListScreenState extends State<ParkingSessionListScreen> {
  int _selectedFilterIndex = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ParkingSessionViewModel>().fetchSessions();
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final List<String> filterLabels = [
      l10n.filterAll,
      l10n.filterOngoing,
      l10n.filterCompleted
    ];

    return Scaffold(
      appBar: AppBar(
        leading: context.canPop()
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => context.pop(),
              )
            : null,
        title: Text(l10n.parkingSessions),
      ),
      body: Consumer<ParkingSessionViewModel>(
        builder: (context, vm, _) {
          return switch (vm.sessionsState) {
            Idle() ||
            Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
              icon: Icons.error_outline,
              title: l10n.dataLoadError,
              subtitle: msg,
            ),
            Success(data: var allSessions) => Builder(
              builder: (context) {
                final sessions = allSessions.where((s) {
                  if (_selectedFilterIndex == 1) return s.isOngoing;
                  if (_selectedFilterIndex == 2) return !s.isOngoing;
                  return true;
                }).toList();

                return Column(
                  children: [
                    // ── Bộ lọc ──
                    SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      padding: const EdgeInsets.symmetric(
                        horizontal: AppTheme.pagePadding,
                        vertical: 8,
                      ),
                      child: Row(
                        children: List.generate(filterLabels.length, (index) {
                          final isSelected = _selectedFilterIndex == index;
                          return Padding(
                            padding: const EdgeInsets.only(right: 8),
                            child: ChoiceChip(
                              label: Text(filterLabels[index]),
                              selected: isSelected,
                              onSelected: (selected) {
                                if (selected) {
                                  setState(() => _selectedFilterIndex = index);
                                }
                              },
                              showCheckmark: false,
                              selectedColor: AppTheme.primary.withValues(
                                alpha: 0.15,
                              ),
                              backgroundColor: AppTheme.surface,
                              labelStyle: TextStyle(
                                color: isSelected
                                    ? AppTheme.primary
                                    : AppTheme.subtle,
                                fontWeight: isSelected
                                    ? FontWeight.w600
                                    : FontWeight.normal,
                              ),
                              side: BorderSide(
                                color: isSelected
                                    ? AppTheme.primary
                                    : AppTheme.border,
                              ),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(20),
                              ),
                            ),
                          );
                        }),
                      ),
                    ),

                    // ── Danh sách ──
                    Expanded(
                      child: sessions.isEmpty
                          ? AppEmptyState(
                              icon: Icons.local_parking_outlined,
                              title: allSessions.isEmpty
                                  ? l10n.noParkingSessions
                                  : l10n.noFilterResults,
                              subtitle: allSessions.isEmpty
                                  ? l10n.parkingHistoryAppearHere
                                  : l10n.noMatchingParkingSession,
                            )
                          : RefreshIndicator(
                              color: AppTheme.primary,
                              onRefresh: () => vm.fetchSessions(),
                              child: ListView.separated(
                                padding: const EdgeInsets.all(
                                  AppTheme.pagePadding,
                                ),
                                itemCount: sessions.length,
                                separatorBuilder: (_, _) =>
                                    const SizedBox(height: 12),
                                itemBuilder: (context, i) => SessionCard(
                                  session: sessions[i],
                                  onTap: () => context.push(
                                    '/customer/parking/${sessions[i].id}',
                                  ),
                                ),
                              ),
                            ),
                    ),
                  ],
                );
              },
            ),
          };
        },
      ),
    );
  }
}
