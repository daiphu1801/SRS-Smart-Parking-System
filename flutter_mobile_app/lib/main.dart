import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/di/service_locator.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_profile/viewmodels/profile_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/add_vehicle_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_parking/viewmodels/parking_session_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_complaint/viewmodels/complaint_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_history/viewmodels/history_viewmodel.dart';

import 'package:smart_parking_mobile/features/customer_payment/viewmodels/payment_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_notification/viewmodels/notification_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_account/viewmodels/group_members_viewmodel.dart';
import 'package:smart_parking_mobile/core/utils/language_viewmodel.dart';

import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  await dotenv.load(fileName: ".env");

  await Supabase.initialize(
    url: dotenv.env['SUPABASE_URL'] ?? '',
    anonKey: dotenv.env['SUPABASE_ANON_KEY'] ?? '',
  );

  setupServiceLocator();
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.dark,
    ),
  );
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthViewModel()),
        ChangeNotifierProvider(create: (_) => ProfileViewModel(sl())),
        ChangeNotifierProvider(create: (_) => BookingViewModel(sl())),
        ChangeNotifierProvider(create: (_) => AddVehicleViewModel(sl())),
        ChangeNotifierProvider(create: (_) => ParkingSessionViewModel(sl())),
        ChangeNotifierProvider(create: (_) => ComplaintViewModel(sl())),
        ChangeNotifierProvider(create: (_) => HistoryViewModel(sl(), sl())),
        ChangeNotifierProvider(create: (_) => PaymentViewModel(sl())),
        ChangeNotifierProvider(create: (_) => NotificationViewModel(sl())),
        ChangeNotifierProvider(create: (_) => GroupMembersViewModel(sl())),
        ChangeNotifierProvider(create: (_) => LanguageViewModel(sl())),
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

    return Consumer<LanguageViewModel>(
      builder: (context, languageViewModel, child) {
        return MaterialApp.router(
          title: 'Smart Parking',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.light,
          routerConfig: router,
          locale: languageViewModel.currentLocale,
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: const [
            Locale('vi'),
            Locale('en'),
          ],
        );
      },
    );
  }
}
