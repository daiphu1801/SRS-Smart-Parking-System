import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_parking/viewmodels/parking_session_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/views/widgets/parking_widgets.dart';

class ParkingSessionListScreen extends StatefulWidget {
  const ParkingSessionListScreen({super.key});

  @override
  State<ParkingSessionListScreen> createState() => _ParkingSessionListScreenState();
}

class _ParkingSessionListScreenState extends State<ParkingSessionListScreen> {
  String _selectedFilter = 'Tất cả';
  final List<String> _filters = ['Tất cả', 'Đang đỗ', 'Hoàn thành'];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ParkingSessionViewModel>().fetchSessions('CUST-001');
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: context.canPop()
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => context.pop(),
              )
            : null,
        title: const Text('Phiên đỗ xe'),
      ),
      body: Consumer<ParkingSessionViewModel>(
        builder: (context, vm, _) {
          return switch (vm.sessionsState) {
            Idle() || Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: 'Lỗi tải dữ liệu',
                subtitle: msg,
              ),
            Success(data: var allSessions) => Builder(builder: (context) {
                final sessions = allSessions.where((s) {
                  if (_selectedFilter == 'Đang đỗ') return s.isOngoing;
                  if (_selectedFilter == 'Hoàn thành') return !s.isOngoing;
                  return true;
                }).toList();

                return Column(
                  children: [
                    // ── Bộ lọc ──
                    SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      padding: const EdgeInsets.symmetric(horizontal: AppTheme.pagePadding, vertical: 8),
                      child: Row(
                        children: _filters.map((filter) {
                          final isSelected = _selectedFilter == filter;
                          return Padding(
                            padding: const EdgeInsets.only(right: 8),
                            child: ChoiceChip(
                              label: Text(filter),
                              selected: isSelected,
                              onSelected: (selected) {
                                if (selected) {
                                  setState(() => _selectedFilter = filter);
                                }
                              },
                              showCheckmark: false,
                              selectedColor: AppTheme.primary.withValues(alpha: 0.15),
                              backgroundColor: AppTheme.surface,
                              labelStyle: TextStyle(
                                color: isSelected ? AppTheme.primary : AppTheme.subtle,
                                fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                              ),
                              side: BorderSide(
                                color: isSelected ? AppTheme.primary : AppTheme.border,
                              ),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(20),
                              ),
                            ),
                          );
                        }).toList(),
                      ),
                    ),

                    // ── Danh sách ──
                    Expanded(
                      child: sessions.isEmpty
                          ? AppEmptyState(
                              icon: Icons.local_parking_outlined,
                              title: allSessions.isEmpty ? 'Chưa có phiên đỗ xe nào' : 'Không có kết quả',
                              subtitle: allSessions.isEmpty
                                  ? 'Lịch sử phiên đỗ xe của bạn sẽ xuất hiện ở đây.'
                                  : 'Không tìm thấy phiên đỗ xe nào phù hợp với bộ lọc.',
                            )
                          : RefreshIndicator(
                              color: AppTheme.primary,
                              onRefresh: () => vm.fetchSessions('CUST-001'),
                              child: ListView.separated(
                                padding: const EdgeInsets.all(AppTheme.pagePadding),
                                itemCount: sessions.length,
                                separatorBuilder: (_, __) => const SizedBox(height: 12),
                                itemBuilder: (context, i) => SessionCard(
                                  session: sessions[i],
                                  onTap: () => context.push('/customer/parking/${sessions[i].id}'),
                                ),
                              ),
                            ),
                    ),
                  ],
                );
              }),
            _ => const SizedBox.shrink(),
          };
        },
      ),
    );
  }
}
