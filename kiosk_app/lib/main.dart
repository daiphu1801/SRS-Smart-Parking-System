import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/di/service_locator.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/language_viewmodel.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/viewmodels/kiosk_payment_viewmodel.dart';
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
        ChangeNotifierProvider(create: (_) => LanguageViewModel(sl())),
        ChangeNotifierProvider.value(value: sl<KioskPaymentViewModel>()),
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
    final router = buildAppRouter(context);

    return Consumer<LanguageViewModel>(
      builder: (context, languageViewModel, child) {
        return MaterialApp.router(
          title: 'Kiosk Payment',
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
