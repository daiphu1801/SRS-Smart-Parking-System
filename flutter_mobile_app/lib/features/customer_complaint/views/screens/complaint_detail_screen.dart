import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_complaint/viewmodels/complaint_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_complaint/models/complaint_models.dart';
import 'package:smart_parking_mobile/core/utils/enum_localizations.dart';

class ComplaintDetailScreen extends StatefulWidget {
  final String complaintId;

  const ComplaintDetailScreen({super.key, required this.complaintId});

  @override
  State<ComplaintDetailScreen> createState() => _ComplaintDetailScreenState();
}

class _ComplaintDetailScreenState extends State<ComplaintDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ComplaintViewModel>().fetchComplaintById(widget.complaintId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Chi tiết Khiếu nại'),
      ),
      body: Consumer<ComplaintViewModel>(
        builder: (context, vm, _) {
          return switch (vm.currentComplaintState) {
            Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: 'Lỗi tải dữ liệu',
                subtitle: msg,
              ),
            Success(data: var complaint) => _ComplaintDetailBody(complaint: complaint),
            _ => const SizedBox.shrink(),
          };
        },
      ),
    );
  }
}

class _ComplaintDetailBody extends StatelessWidget {
  final Complaint complaint;

  const _ComplaintDetailBody({required this.complaint});

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');

    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppTheme.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // ── Header Status ──────────────────────────────────────────────────
          Container(
            padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 20),
            decoration: BoxDecoration(
              color: complaint.status.color.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(AppTheme.radiusCard),
              border: Border.all(color: complaint.status.color.withValues(alpha: 0.3)),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Trạng thái',
                      style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      complaint.status.label(context),
                      style: AppTheme.heading3.copyWith(
                        color: complaint.status.color,
                        fontSize: 18,
                      ),
                    ),
                  ],
                ),
                Icon(
                  _getStatusIcon(complaint.status),
                  size: 36,
                  color: complaint.status.color,
                ),
              ],
            ),
          ),

          const SizedBox(height: AppTheme.sectionGap),

          // ── Nội dung khiếu nại ───────────────────────────────────────────
          Text('Nội dung phản ánh', style: AppTheme.heading3),
          const SizedBox(height: 12),
          AppCard(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Mã: ${complaint.id}',
                        style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                    Text(dateFormatter.format(complaint.createdAt),
                        style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                  ],
                ),
                const Divider(height: 24),
                Text(
                  complaint.title,
                  style: AppTheme.heading3.copyWith(fontSize: 18),
                ),
                const SizedBox(height: 12),
                Text(
                  complaint.description,
                  style: AppTheme.body.copyWith(height: 1.5),
                ),
                if (complaint.imageUrl != null) ...[
                  const SizedBox(height: 16),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: Image.network(
                      complaint.imageUrl!,
                      width: double.infinity,
                      height: 200,
                      fit: BoxFit.cover,
                      errorBuilder: (context, error, stackTrace) => Container(
                        height: 200,
                        color: AppTheme.surface,
                        child: const Center(
                          child: Icon(Icons.broken_image_outlined,
                              size: 40, color: Colors.grey),
                        ),
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ),

          const SizedBox(height: AppTheme.sectionGap),

          // ── Kết quả xử lý ────────────────────────────────────────────────
          Text('Kết quả xử lý', style: AppTheme.heading3),
          const SizedBox(height: 12),
          AppCard(
            padding: const EdgeInsets.all(16),
            child: complaint.resolutionNote != null && complaint.resolutionNote!.isNotEmpty
                ? Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(Icons.admin_panel_settings_outlined,
                              size: 18, color: AppTheme.primary),
                          const SizedBox(width: 8),
                          Text('Ban quản lý phản hồi',
                              style: AppTheme.heading3.copyWith(fontSize: 16)),
                        ],
                      ),
                      if (complaint.updatedAt != null) ...[
                        const SizedBox(height: 4),
                        Text(
                          dateFormatter.format(complaint.updatedAt!),
                          style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                        ),
                      ],
                      const Divider(height: 24),
                      Text(
                        complaint.resolutionNote!,
                        style: AppTheme.body.copyWith(height: 1.5),
                      ),
                    ],
                  )
                : Center(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 20),
                      child: Text(
                        'Ban quản lý đang tiếp nhận và xử lý khiếu nại của bạn. Xin vui lòng chờ thêm.',
                        style: AppTheme.body.copyWith(
                          color: AppTheme.subtle,
                          fontStyle: FontStyle.italic,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
          ),
        ],
      ),
    );
  }

  IconData _getStatusIcon(ComplaintStatus status) {
    return switch (status) {
      ComplaintStatus.pending => Icons.hourglass_empty_rounded,
      ComplaintStatus.processing => Icons.autorenew_rounded,
      ComplaintStatus.resolved => Icons.check_circle_outline_rounded,
      ComplaintStatus.rejected => Icons.cancel_outlined,
    };
  }
}
