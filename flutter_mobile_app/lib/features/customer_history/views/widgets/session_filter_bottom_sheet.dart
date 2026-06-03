import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

class FilterBottomSheet extends StatefulWidget {
  final DateTime? initialStartDate;
  final DateTime? initialEndDate;
  final String? initialPlateNumber;

  const FilterBottomSheet({
    super.key,
    this.initialStartDate,
    this.initialEndDate,
    this.initialPlateNumber,
  });

  @override
  State<FilterBottomSheet> createState() => _FilterBottomSheetState();
}

class _FilterBottomSheetState extends State<FilterBottomSheet> {
  DateTime? _startDate;
  DateTime? _endDate;
  late TextEditingController _plateController;

  @override
  void initState() {
    super.initState();
    _startDate = widget.initialStartDate;
    _endDate = widget.initialEndDate;
    _plateController = TextEditingController(
      text: widget.initialPlateNumber ?? '',
    );
  }

  @override
  void dispose() {
    _plateController.dispose();
    super.dispose();
  }

  Future<void> _pickDate(bool isStart) async {
    final picked = await showDatePicker(
      context: context,
      initialDate: isStart
          ? (_startDate ?? DateTime.now())
          : (_endDate ?? DateTime.now()),
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
      builder: (ctx, child) => Theme(
        data: Theme.of(ctx).copyWith(
          colorScheme: const ColorScheme.light(
            primary: AppTheme.primary,
            onPrimary: Colors.white,
            onSurface: Colors.black,
          ),
        ),
        child: child!,
      ),
    );
    if (picked == null) return;
    setState(() {
      if (isStart) {
        _startDate = picked;
        if (_endDate != null && _endDate!.isBefore(_startDate!)) {
          _endDate = _startDate;
        }
      } else {
        _endDate = picked;
        if (_startDate != null && _startDate!.isAfter(_endDate!)) {
          _startDate = _endDate;
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy');

    return Padding(
      padding: EdgeInsets.only(
        left: 24,
        right: 24,
        top: 24,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(AppLocalizations.of(context)!.searchFilterTitle, style: AppTheme.heading2),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Text(AppLocalizations.of(context)!.timeRangeLabel, style: AppTheme.heading3),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: _DatePickerField(
                  label: _startDate != null
                      ? dateFormatter.format(_startDate!)
                      : AppLocalizations.of(context)!.fromDateLabel,
                  onTap: () => _pickDate(true),
                ),
              ),
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 12),
                child: Icon(Icons.arrow_forward, size: 20, color: Colors.grey),
              ),
              Expanded(
                child: _DatePickerField(
                  label: _endDate != null
                      ? dateFormatter.format(_endDate!)
                      : AppLocalizations.of(context)!.toDateLabel,
                  onTap: () => _pickDate(false),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Text(AppLocalizations.of(context)!.plateNumberLabel, style: AppTheme.heading3),
          const SizedBox(height: 12),
          TextFormField(
            controller: _plateController,
            decoration: InputDecoration(
              hintText: AppLocalizations.of(context)!.plateNumberExample,
              border: const OutlineInputBorder(),
              contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
            ),
          ),
          const SizedBox(height: 32),
          Row(
            children: [
              Expanded(
                child: AppOutlinedButton(
                  label: AppLocalizations.of(context)!.resetFilterButton,
                  onPressed: () => setState(() {
                    _startDate = null;
                    _endDate = null;
                    _plateController.clear();
                  }),
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: AppFilledButton(
                  label: AppLocalizations.of(context)!.applyFilterButton,
                  onPressed: () => Navigator.pop(context, {
                    'startDate': _startDate,
                    'endDate': _endDate,
                    'plateNumber': _plateController.text,
                  }),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _DatePickerField extends StatelessWidget {
  final String label;
  final VoidCallback onTap;
  const _DatePickerField({required this.label, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
        decoration: BoxDecoration(
          border: Border.all(color: AppTheme.border),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Text(
                label,
                style: AppTheme.body.copyWith(
                  color: label.contains('/') ? null : AppTheme.subtle,
                ),
                overflow: TextOverflow.ellipsis,
              ),
            ),
            const SizedBox(width: 4),
            Icon(Icons.calendar_today, size: 16, color: AppTheme.subtle),
          ],
        ),
      ),
    );
  }
}
