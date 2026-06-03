import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/features/customer_history/viewmodels/history_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_history/views/widgets/payment_tab.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

/// Màn hình Lịch sử — theo chuẩn MVVM của dự án.
class CustomerHistoryScreen extends StatefulWidget {
  const CustomerHistoryScreen({super.key});

  @override
  State<CustomerHistoryScreen> createState() => _CustomerHistoryScreenState();
}

class _CustomerHistoryScreenState extends State<CustomerHistoryScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final vm = context.read<HistoryViewModel>();
      vm.fetchPayments();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(AppLocalizations.of(context)!.historyTitle, style: AppTheme.heading1),
      ),
      body: const PaymentTab(),
    );
  }
}
