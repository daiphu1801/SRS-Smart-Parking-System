import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

class SessionCard extends StatelessWidget {
  final ParkingSession session;
  final VoidCallback? onTap;

  const SessionCard({super.key, required this.session, this.onTap});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');
    final isOngoing = session.isOngoing;
    final isCar = session.vehicleType == 'CAR';

    final isVi = Localizations.localeOf(context).languageCode == 'vi';
    final durationText = isVi 
        ? session.durationFormatted 
        : session.durationFormatted.replaceAll('g', 'h').replaceAll('ph', 'm');

    final statusText = isOngoing ? l10n.filterOngoing : l10n.filterCompleted;

    return Container(
      decoration: BoxDecoration(
        color: AppTheme.surface,
        borderRadius: BorderRadius.circular(AppTheme.radiusCard),
        border: Border.all(color: AppTheme.border),
        boxShadow: AppTheme.shadowSmall,
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(AppTheme.radiusCard),
        child: Container(
          decoration: BoxDecoration(
            border: Border(
              left: BorderSide(
                color: isOngoing ? AppTheme.primary : Colors.grey.shade400,
                width: 4,
              ),
            ),
          ),
          child: Material(
            color: Colors.transparent,
            child: InkWell(
              onTap: onTap,
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // ── Header: biển số + badge trạng thái ──────────────────────────
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.all(8),
                                decoration: BoxDecoration(
                                  color: isCar
                                      ? Colors.blue.shade50
                                      : Colors.teal.shade50,
                                  shape: BoxShape.circle,
                                ),
                                child: Icon(
                                  isCar
                                      ? Icons.directions_car
                                      : Icons.two_wheeler,
                                  size: 18,
                                  color: isCar
                                      ? Colors.blue.shade700
                                      : Colors.teal.shade700,
                                ),
                              ),
                              const SizedBox(width: 12),
                              Flexible(
                                child: Text(
                                  session.plateNumber,
                                  style: AppTheme.heading3.copyWith(
                                    fontSize: 16,
                                  ),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              if (session.flagManual) ...[
                                const SizedBox(width: 6),
                                Tooltip(
                                  message: l10n.manualGateOpen,
                                  child: Icon(
                                    Icons.warning_amber_outlined,
                                    size: 16,
                                    color: Colors.orange.shade700,
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                        const SizedBox(width: 8),
                        AppBadge(
                          label: statusText,
                          isFilled: isOngoing,
                        ),
                      ],
                    ),

                    const SizedBox(height: 12),

                    // ── Cổng vào/ra ─────────────────────────────────────────────────
                    if (session.zoneInName != null)
                      _buildRow(
                        Icons.login_rounded,
                        l10n.entryLabel,
                        '${dateFormatter.format(session.entryTime)} · ${session.zoneInName}',
                      ),
                    const SizedBox(height: 4),
                    _buildRow(
                      Icons.logout_rounded,
                      l10n.exitLabel,
                      session.exitTime != null
                          ? '${dateFormatter.format(session.exitTime!)} · ${session.zoneOutName ?? "—"}'
                          : l10n.vehicleInLot,
                    ),
                    const SizedBox(height: 4),
                    _buildRow(
                      Icons.timer_outlined,
                      l10n.sessionDurationLabel,
                      durationText,
                    ),

                    // ── Banner ân hạn ────────────────────────────────────────────────
                    if (session.isInGracePeriod) ...[
                      const SizedBox(height: 12),
                      GracePeriodBanner(session: session),
                    ],
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildRow(IconData icon, String label, String value) {
    return Row(
      children: [
        Icon(icon, size: 14, color: AppTheme.subtle),
        const SizedBox(width: 6),
        Text(label, style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
        const SizedBox(width: 4),
        Expanded(
          child: Text(
            value,
            style: AppTheme.bodySmall.copyWith(fontWeight: FontWeight.w600),
            textAlign: TextAlign.right,
          ),
        ),
      ],
    );
  }
}

class GracePeriodBanner extends StatelessWidget {
  final ParkingSession session;
  const GracePeriodBanner({super.key, required this.session});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final remaining = session.gracePeriodRemaining ?? Duration.zero;
    final mins = remaining.inMinutes;
    final secs = remaining.inSeconds.remainder(60).toString().padLeft(2, '0');
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Row(
        children: [
          Icon(Icons.timer_outlined, size: 14, color: Colors.orange.shade700),
          const SizedBox(width: 6),
          Expanded(
            child: Text(
              l10n.gracePeriodPrompt(mins, secs),
              style: AppTheme.bodySmall.copyWith(
                color: Colors.orange.shade700,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class DetailInfoCard extends StatelessWidget {
  final String title;
  final List<DetailRowItem> items;

  const DetailInfoCard({super.key, required this.title, required this.items});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: AppTheme.heading3),
        const SizedBox(height: 12),
        AppCard(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: items.asMap().entries.map((entry) {
              final idx = entry.key;
              final item = entry.value;
              return Column(
                children: [
                  _DetailRowWidget(item: item),
                  if (idx < items.length - 1) const Divider(height: 24),
                ],
              );
            }).toList(),
          ),
        ),
      ],
    );
  }
}

class DetailRowItem {
  final IconData? icon;
  final Widget? leading;
  final String label;
  final String value;
  final TextStyle? valueStyle;

  DetailRowItem({
    this.icon,
    this.leading,
    required this.label,
    required this.value,
    this.valueStyle,
  });
}

class _DetailRowWidget extends StatelessWidget {
  final DetailRowItem item;

  const _DetailRowWidget({required this.item});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (item.leading != null)
          item.leading!
        else if (item.icon != null)
          Icon(item.icon, size: 18, color: AppTheme.subtle),
        if (item.leading != null || item.icon != null)
          const SizedBox(width: 12),
        Expanded(
          child: Text(
            item.label,
            style: AppTheme.body.copyWith(color: AppTheme.subtle),
          ),
        ),
        Flexible(
          child: Text(
            item.value,
            style:
                item.valueStyle ??
                AppTheme.body.copyWith(fontWeight: FontWeight.w600),
            textAlign: TextAlign.right,
          ),
        ),
      ],
    );
  }
}
