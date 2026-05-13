import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/di/service_locator.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_account/viewmodels/customer_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_parking/viewmodels/parking_session_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_complaint/viewmodels/complaint_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_history/viewmodels/history_viewmodel.dart';

import 'package:smart_parking_mobile/features/customer_payment/viewmodels/payment_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_notification/viewmodels/notification_viewmodel.dart';

import 'package:smart_parking_mobile/features/customer_account/viewmodels/group_accounts_viewmodel.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  setupServiceLocator();
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.dark,
  ));
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthViewModel()),
        ChangeNotifierProvider(create: (_) => CustomerViewModel()),
        ChangeNotifierProvider(create: (_) => BookingViewModel()),
        ChangeNotifierProvider(create: (_) => ParkingSessionViewModel()),
        ChangeNotifierProvider(create: (_) => ComplaintViewModel()),
        ChangeNotifierProvider(create: (_) => HistoryViewModel()),
        ChangeNotifierProvider(create: (_) => PaymentViewModel()),
        ChangeNotifierProvider(create: (_) => NotificationViewModel()),
        ChangeNotifierProvider(create: (_) => GroupAccountsViewModel()),
      ],
      child: const SmartParkingApp(),
    ),
  );
}

class SmartParkingApp extends StatefulWidget {
  const SmartParkingApp({super.key});

  @override
  State<SmartParkingApp> createState() => _SmartParkingAppState();
}

class _SmartParkingAppState extends State<SmartParkingApp> {
  @override
  Widget build(BuildContext context) {
    // Router is built using the provided AuthViewModel to handle redirects automatically.
    final authViewModel = context.read<AuthViewModel>();
    final router = buildAppRouter(context, authViewModel);

    return MaterialApp.router(
      title: 'Smart Parking',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      routerConfig: router,
    );
  }
}
