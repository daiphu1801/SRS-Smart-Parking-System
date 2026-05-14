import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';

/// Step Indicator row used in Auth flows
class AuthStepIndicator extends StatelessWidget {
  final int currentStep;
  final int totalSteps;
  final List<String> labels;

  const AuthStepIndicator({
    super.key,
    required this.currentStep,
    this.totalSteps = 3,
    this.labels = const ['SDT', 'OTP', 'Mật khẩu'],
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(totalSteps * 2 - 1, (index) {
        if (index.isEven) {
          final stepNum = index ~/ 2 + 1;
          return _StepCircle(
            step: stepNum,
            label: labels[stepNum - 1],
            isActive: currentStep == stepNum,
            isCompleted: currentStep > stepNum,
          );
        } else {
          return Container(
            width: 20,
            height: 1,
            color: AppTheme.border,
            margin: const EdgeInsets.symmetric(horizontal: 8),
          );
        }
      }),
    );
  }
}

class _StepCircle extends StatelessWidget {
  final int step;
  final String label;
  final bool isActive;
  final bool isCompleted;

  const _StepCircle({
    required this.step,
    required this.label,
    required this.isActive,
    required this.isCompleted,
  });

  @override
  Widget build(BuildContext context) {
    final Color circleColor = isActive 
        ? AppTheme.primary 
        : (isCompleted ? AppTheme.primary.withValues(alpha: 0.6) : AppTheme.surface);
    
    final Color borderColor = (isActive || isCompleted) 
        ? AppTheme.primary 
        : AppTheme.border;

    return Column(
      children: [
        Container(
          width: 32,
          height: 32,
          decoration: BoxDecoration(
            color: circleColor,
            border: Border.all(color: borderColor, width: 2),
            shape: BoxShape.circle,
          ),
          child: Center(
            child: isCompleted 
                ? const Icon(Icons.check, size: 16, color: Colors.white)
                : Text(
                    '$step',
                    style: AppTheme.bodySmall.copyWith(
                      fontWeight: FontWeight.w700,
                      color: (isActive || isCompleted) ? Colors.white : AppTheme.subtle,
                    ),
                  ),
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label, 
          style: AppTheme.caption.copyWith(
            color: isActive ? AppTheme.primary : AppTheme.subtle,
            fontWeight: isActive ? FontWeight.w600 : FontWeight.normal,
          ),
        ),
      ],
    );
  }
}

/// Header for Auth screens with logo and title
class AuthHeader extends StatelessWidget {
  final String title;
  final String? subtitle;

  const AuthHeader({
    super.key,
    required this.title,
    this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Center(
          child: Icon(Icons.local_parking_rounded, size: 64, color: AppTheme.primary),
        ),
        const SizedBox(height: 12),
        Center(child: Text('Smart Parking', style: AppTheme.heading2)),
        if (title.isNotEmpty) ...[
          const SizedBox(height: 32),
          Align(
            alignment: Alignment.centerLeft,
            child: Text(title, style: AppTheme.heading3),
          ),
        ],
        if (subtitle != null) ...[
          const SizedBox(height: 8),
          Align(
            alignment: Alignment.centerLeft,
            child: Text(
              subtitle!,
              style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
            ),
          ),
        ],
      ],
    );
  }
}
