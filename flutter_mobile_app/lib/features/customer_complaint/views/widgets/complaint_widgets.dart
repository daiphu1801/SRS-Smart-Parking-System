import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_complaint/models/complaint_models.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

/// Card showing complaint overview in lists
class ComplaintCard extends StatelessWidget {
  final Complaint complaint;

  const ComplaintCard({super.key, required this.complaint});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');

    return AppCard(
      onTap: () => context.push('/customer/complaints/${complaint.id}'),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Text(
                  complaint.title,
                  style: AppTheme.heading3.copyWith(fontSize: 16),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              const SizedBox(width: 8),
              ComplaintStatusBadge(status: complaint.status),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            complaint.description,
            style: AppTheme.body.copyWith(color: AppTheme.subtle),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(Icons.access_time, size: 14, color: AppTheme.subtle),
                  const SizedBox(width: 4),
                  Text(
                    dateFormatter.format(complaint.createdAt),
                    style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                  ),
                ],
              ),
              Text(
                l10n.complaintCodeLabel(complaint.id),
                style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// Colored badge for complaint status
class ComplaintStatusBadge extends StatelessWidget {
  final ComplaintStatus status;

  const ComplaintStatusBadge({super.key, required this.status});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final statusText = switch (status) {
      ComplaintStatus.pending => l10n.complaintStatusPending,
      ComplaintStatus.processing => l10n.complaintStatusProcessing,
      ComplaintStatus.resolved => l10n.complaintStatusResolved,
      ComplaintStatus.rejected => l10n.complaintStatusRejected,
    };

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: status.color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: status.color.withValues(alpha: 0.5)),
      ),
      child: Text(
        statusText,
        style: AppTheme.caption.copyWith(
          color: status.color,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}
