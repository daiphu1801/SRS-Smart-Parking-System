import 'package:flutter/material.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../../core/widgets/app_widgets.dart';

class PaymentInfoCard extends StatelessWidget {
  final String title;
  final List<PaymentRowItem> items;

  const PaymentInfoCard({
    super.key,
    required this.title,
    required this.items,
  });

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
              final index = entry.key;
              final item = entry.value;
              return Column(
                children: [
                  _PaymentDetailRow(item: item),
                  if (index < items.length - 1) const Divider(height: 24),
                ],
              );
            }).toList(),
          ),
        ),
      ],
    );
  }
}

class PaymentRowItem {
  final IconData? icon;
  final Widget? leading;
  final String label;
  final String value;
  final TextStyle? valueStyle;

  const PaymentRowItem({
    this.icon,
    this.leading,
    required this.label,
    required this.value,
    this.valueStyle,
  });
}

class _PaymentDetailRow extends StatelessWidget {
  final PaymentRowItem item;
  const _PaymentDetailRow({required this.item});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (item.leading != null)
          item.leading!
        else if (item.icon != null)
          Icon(item.icon, size: 18, color: AppTheme.subtle),
        if (item.leading != null || item.icon != null) const SizedBox(width: 12),
        Expanded(
          child: Text(
            item.label,
            style: AppTheme.body.copyWith(color: AppTheme.subtle),
          ),
        ),
        Flexible(
          child: Text(
            item.value,
            style: item.valueStyle ?? AppTheme.body.copyWith(fontWeight: FontWeight.w600),
            textAlign: TextAlign.right,
          ),
        ),
      ],
    );
  }
}
