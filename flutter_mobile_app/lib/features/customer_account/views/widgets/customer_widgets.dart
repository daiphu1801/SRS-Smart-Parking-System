import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';

class InfoRow extends StatelessWidget {
  final String label;
  final String value;
  final Widget? trailing;

  const InfoRow({
    super.key,
    required this.label,
    required this.value,
    this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
        const SizedBox(width: 16),
        Expanded(
          child: trailing ??
              Text(
                value,
                style: AppTheme.body,
                textAlign: TextAlign.right,
              ),
        ),
      ],
    );
  }
}

class MenuCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  const MenuCard({
    super.key,
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return AppCard(
      onTap: onTap,
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppTheme.primary.withValues(alpha: 0.1),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: AppTheme.primary),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: AppTheme.heading3),
                const SizedBox(height: 4),
                Text(subtitle, style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
              ],
            ),
          ),
          Icon(Icons.chevron_right, color: AppTheme.subtle),
        ],
      ),
    );
  }
}

class QuickInfoItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const QuickInfoItem({
    super.key,
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Icon(icon, color: AppTheme.subtle, size: 20),
        const SizedBox(height: 4),
        Text(label, style: AppTheme.caption.copyWith(color: AppTheme.subtle)),
        const SizedBox(height: 2),
        Text(value, style: AppTheme.body.copyWith(fontWeight: FontWeight.w600)),
      ],
    );
  }
}

class AccountMemberCard extends StatelessWidget {
  final String fullName;
  final String phone;
  final String? badgeLabel;
  final bool isBadgeFilled;
  final Widget? trailing;

  const AccountMemberCard({
    super.key,
    required this.fullName,
    required this.phone,
    this.badgeLabel,
    this.isBadgeFilled = false,
    this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    return AppCard(
      padding: const EdgeInsets.all(12),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: AppTheme.primary.withValues(alpha: 0.1),
              shape: BoxShape.circle,
            ),
            alignment: Alignment.center,
            child: Text(
              fullName.isNotEmpty ? fullName[0].toUpperCase() : '?',
              style: AppTheme.heading3.copyWith(color: AppTheme.primary),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(fullName, style: AppTheme.heading3.copyWith(fontSize: 15)),
                const SizedBox(height: 4),
                Text(phone, style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
              ],
            ),
          ),
          if (badgeLabel != null)
            AppBadge(label: badgeLabel!, isFilled: isBadgeFilled),
          if (trailing != null) trailing!,
        ],
      ),
    );
  }
}
