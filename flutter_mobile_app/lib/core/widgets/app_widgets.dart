import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';

/// Primary (Filled) Button — background #052e16, text white
class AppFilledButton extends StatelessWidget {
  final String label;
  final VoidCallback? onPressed;
  final bool isLoading;
  final double? width;

  const AppFilledButton({
    super.key,
    required this.label,
    this.onPressed,
    this.isLoading = false,
    this.width,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: width ?? double.infinity,
      height: AppTheme.buttonHeight,
      child: ElevatedButton(
        onPressed: isLoading ? null : onPressed,
        child: isLoading
            ? SizedBox(
                width: 18,
                height: 18,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  color: AppTheme.background,
                ),
              )
            : Text(label, style: AppTheme.label.copyWith(color: AppTheme.background)),
      ),
    );
  }
}

/// Secondary (Outlined) Button — background white, border #052e16
class AppOutlinedButton extends StatelessWidget {
  final String label;
  final VoidCallback? onPressed;
  final double? width;

  const AppOutlinedButton({
    super.key,
    required this.label,
    this.onPressed,
    this.width,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: width ?? double.infinity,
      height: AppTheme.buttonHeight,
      child: OutlinedButton(
        onPressed: onPressed,
        child: Text(label, style: AppTheme.label),
      ),
    );
  }
}

/// App Input Field following design spec
class AppTextField extends StatelessWidget {
  final String label;
  final String? placeholder;
  final TextEditingController? controller;
  final TextInputType keyboardType;
  final bool obscureText;
  final String? errorText;
  final String? helperText;
  final bool readOnly;
  final TextCapitalization textCapitalization;
  final Widget? suffixIcon;
  final ValueChanged<String>? onChanged;
  final String? Function(String?)? validator;
  final int? maxLines;
  final int? minLines;

  const AppTextField({
    super.key,
    required this.label,
    String? placeholder,
    String? hint, // Alias for placeholder
    this.controller,
    this.keyboardType = TextInputType.text,
    this.obscureText = false,
    this.textCapitalization = TextCapitalization.none,
    this.errorText,
    this.helperText,
    this.readOnly = false,
    this.suffixIcon,
    this.onChanged,
    this.validator,
    this.maxLines = 1,
    this.minLines,
  }) : placeholder = hint ?? placeholder;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: AppTheme.label),
        const SizedBox(height: 6),
        TextFormField(
          controller: controller,
          keyboardType: keyboardType,
          obscureText: obscureText,
          textCapitalization: textCapitalization,
          readOnly: readOnly,
          onChanged: onChanged,
          validator: validator,
          maxLines: maxLines,
          minLines: minLines,
          style: AppTheme.body,
          decoration: InputDecoration(
            hintText: placeholder,
            errorText: errorText,
            helperText: helperText,
            suffixIcon: suffixIcon,
            constraints: const BoxConstraints(minHeight: 44),
          ),
        ),
      ],
    );
  }
}

/// App Card — white, 1px border, rounded 8px, NO shadow
class AppCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry? padding;
  final VoidCallback? onTap;
  final Color? color;

  const AppCard({
    super.key,
    required this.child,
    this.padding,
    this.onTap,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(AppTheme.radiusCard),
      child: Container(
        padding: padding ?? const EdgeInsets.all(AppTheme.cardPadding),
        decoration: BoxDecoration(
          color: color ?? AppTheme.background,
          borderRadius: BorderRadius.circular(AppTheme.radiusCard),
          border: Border.all(color: AppTheme.border),
        ),
        child: child,
      ),
    );
  }
}

/// Status Badge (11px, border, rounded 4px)
class AppBadge extends StatelessWidget {
  final String label;
  final bool isFilled;
  final Color? color;

  const AppBadge({super.key, required this.label, this.isFilled = false, this.color});

  @override
  Widget build(BuildContext context) {
    final badgeColor = color ?? AppTheme.primary;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: isFilled ? badgeColor : AppTheme.background,
        borderRadius: BorderRadius.circular(AppTheme.radiusBadge),
        border: Border.all(color: badgeColor),
      ),
      child: Text(
        label,
        style: AppTheme.caption.copyWith(
          color: isFilled ? AppTheme.background : badgeColor,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}

/// Empty state widget
class AppEmptyState extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? subtitle;
  final Widget? action;

  const AppEmptyState({super.key, required this.icon, required this.title, this.subtitle, this.action});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 40, color: AppTheme.disabled),
            const SizedBox(height: 12),
            Text(title, style: AppTheme.heading3, textAlign: TextAlign.center),
            if (subtitle != null) ...[
              const SizedBox(height: 6),
              Text(subtitle!, style: AppTheme.body.copyWith(color: AppTheme.subtle), textAlign: TextAlign.center),
            ],
            if (action != null) ...[const SizedBox(height: 20), action!],
          ],
        ),
      ),
    );
  }
}
