import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_parking/viewmodels/parking_session_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/views/widgets/parking_widgets.dart';

class ParkingSessionDetailScreen extends StatefulWidget {
  final String sessionId;
  const ParkingSessionDetailScreen({super.key, required this.sessionId});

  @override
  State<ParkingSessionDetailScreen> createState() => _ParkingSessionDetailScreenState();
}

class _ParkingSessionDetailScreenState extends State<ParkingSessionDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ParkingSessionViewModel>().fetchSessionById(widget.sessionId);
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
        title: const Text('Chi tiết phiên đỗ xe'),
      ),
      body: Consumer<ParkingSessionViewModel>(
        builder: (context, vm, _) {
          return switch (vm.currentSessionState) {
            Idle() || Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: 'Lỗi tải dữ liệu',
                subtitle: msg,
              ),
            Success(data: var session) => _SessionDetailBody(session: session),
            _ => const SizedBox.shrink(),
          };
        },
      ),
    );
  }
}

class _SessionDetailBody extends StatelessWidget {
  final ParkingSession session;
  const _SessionDetailBody({required this.session});

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm:ss');

    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppTheme.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // ── Status Banner ────────────────────────────────────────────────
          _StatusBanner(session: session),

          const SizedBox(height: AppTheme.sectionGap),

          // ── Cảnh báo thời gian ân hạn ───────────────────────────────────
          if (session.isInGracePeriod) ...[
            _GracePeriodCard(session: session),
            const SizedBox(height: AppTheme.sectionGap),
          ],

          // ── Ảnh Camera LPR ───────────────────────────────────────────────
          Text('Ảnh Camera biển số xe', style: AppTheme.heading3),
          const SizedBox(height: 12),
          _LprImageRow(session: session),

          const SizedBox(height: AppTheme.sectionGap),

          // ── Thông tin xe ─────────────────────────────────────────────────
          DetailInfoCard(
            title: 'Thông tin xe',
            items: [
              DetailRowItem(
                leading: Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    color: session.vehicleType == 'CAR' ? Colors.blue.shade50 : Colors.teal.shade50,
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    session.vehicleType == 'CAR' ? Icons.directions_car : Icons.two_wheeler,
                    size: 16,
                    color: session.vehicleType == 'CAR' ? Colors.blue.shade700 : Colors.teal.shade700,
                  ),
                ),
                label: 'Biển số xe',
                value: session.plateNumber,
                valueStyle: AppTheme.heading3.copyWith(fontSize: 16),
              ),
              DetailRowItem(
                icon: Icons.category_outlined,
                label: 'Loại xe',
                value: session.vehicleType == 'CAR' ? 'Ô tô' : 'Xe máy',
              ),
              if (session.bookingDetailId != null)
                DetailRowItem(
                  icon: Icons.card_membership_outlined,
                  label: 'Mã gói cước',
                  value: session.bookingDetailId!,
                ),
              if (session.flagManual)
                DetailRowItem(
                  icon: Icons.warning_amber_outlined,
                  label: 'Lưu ý',
                  value: 'Cổng mở thủ công',
                  valueStyle: AppTheme.body.copyWith(color: Colors.orange.shade700),
                ),
            ],
          ),

          const SizedBox(height: 20),

          // ── Thông tin cổng ───────────────────────────────────────────────
          DetailInfoCard(
            title: 'Cổng vào / ra',
            items: [
              DetailRowItem(
                icon: Icons.login_rounded,
                label: 'Cổng vào',
                value: session.zoneInName ?? '—',
              ),
              DetailRowItem(
                icon: Icons.logout_rounded,
                label: 'Cổng ra',
                value: session.zoneOutName ?? '— Chưa ra',
              ),
            ],
          ),

          const SizedBox(height: 20),

          // ── Thời gian đỗ ─────────────────────────────────────────────────
          DetailInfoCard(
            title: 'Thời gian đỗ xe',
            items: [
              DetailRowItem(
                icon: Icons.login_outlined,
                label: 'Thời gian vào',
                value: dateFormatter.format(session.entryTime),
              ),
              DetailRowItem(
                icon: Icons.logout_outlined,
                label: 'Thời gian ra',
                value: session.exitTime != null ? dateFormatter.format(session.exitTime!) : '— Xe chưa ra khỏi bãi',
              ),
              DetailRowItem(
                icon: Icons.hourglass_bottom_outlined,
                label: 'Tổng thời gian',
                value: session.durationFormatted,
                valueStyle: AppTheme.heading3.copyWith(color: AppTheme.primary),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _StatusBanner extends StatelessWidget {
  final ParkingSession session;
  const _StatusBanner({required this.session});

  @override
  Widget build(BuildContext context) {
    final isOngoing = session.isOngoing;
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 20),
      decoration: BoxDecoration(
        color: isOngoing ? AppTheme.primary : AppTheme.surface,
        borderRadius: BorderRadius.circular(AppTheme.radiusCard),
        border: Border.all(color: AppTheme.border),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                isOngoing ? 'Đang đỗ xe' : 'Đã hoàn thành',
                style: AppTheme.heading3.copyWith(
                  color: isOngoing ? AppTheme.background : AppTheme.primary,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'Mã: ${session.id}',
                style: AppTheme.bodySmall.copyWith(
                  color: isOngoing ? AppTheme.background.withValues(alpha: 0.8) : AppTheme.subtle,
                ),
              ),
            ],
          ),
          Icon(
            isOngoing ? Icons.local_parking : Icons.check_circle_outline,
            size: 36,
            color: isOngoing ? AppTheme.background : AppTheme.primary,
          ),
        ],
      ),
    );
  }
}

class _GracePeriodCard extends StatelessWidget {
  final ParkingSession session;
  const _GracePeriodCard({required this.session});

  @override
  Widget build(BuildContext context) {
    final remaining = session.gracePeriodRemaining ?? Duration.zero;
    final mins = remaining.inMinutes;
    final secs = remaining.inSeconds.remainder(60).toString().padLeft(2, '0');

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(AppTheme.radiusCard),
        border: Border.all(color: Colors.orange.shade300),
      ),
      child: Row(
        children: [
          Icon(Icons.timer_outlined, size: 24, color: Colors.orange.shade700),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Vui lòng lấy xe ra khỏi bãi',
                  style: AppTheme.body.copyWith(
                    color: Colors.orange.shade800,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  'Bạn còn ${mins}ph ${secs}s để rời bãi đỗ',
                  style: AppTheme.bodySmall.copyWith(color: Colors.orange.shade700),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _LprImageRow extends StatelessWidget {
  final ParkingSession session;
  const _LprImageRow({required this.session});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(child: _LprImageBox(url: session.imageInUrl, label: 'Lúc vào')),
        const SizedBox(width: 12),
        Expanded(
          child: _LprImageBox(
            url: session.imageOutUrl,
            label: 'Lúc ra',
            isEmpty: session.imageOutUrl == null,
          ),
        ),
      ],
    );
  }
}

class _LprImageBox extends StatelessWidget {
  final String? url;
  final String label;
  final bool isEmpty;

  const _LprImageBox({this.url, required this.label, this.isEmpty = false});

  @override
  Widget build(BuildContext context) {
    return AppCard(
      padding: EdgeInsets.zero,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          ClipRRect(
            borderRadius: const BorderRadius.vertical(top: Radius.circular(AppTheme.radiusCard)),
            child: SizedBox(
              height: 100,
              child: isEmpty || url == null
                  ? Container(
                      color: AppTheme.surface,
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.image_not_supported_outlined, size: 28, color: AppTheme.subtle),
                          const SizedBox(height: 4),
                          Text('Chưa có ảnh', style: AppTheme.caption.copyWith(color: AppTheme.subtle)),
                        ],
                      ),
                    )
                  : Image.network(
                      url!,
                      fit: BoxFit.cover,
                      errorBuilder: (context, error, stackTrace) => Container(
                        color: AppTheme.surface,
                        child: const Icon(Icons.broken_image_outlined, size: 28, color: Colors.grey),
                      ),
                      loadingBuilder: (context, child, loadingProgress) {
                        if (loadingProgress == null) return child;
                        return Container(
                          color: AppTheme.surface,
                          child: const Center(
                            child: SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            ),
                          ),
                        );
                      },
                    ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
            child: Text(
              label,
              textAlign: TextAlign.center,
              style: AppTheme.bodySmall.copyWith(
                color: AppTheme.subtle,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
