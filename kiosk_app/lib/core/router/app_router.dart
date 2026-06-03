import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/views/input_plate_screen.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/views/qr_payment_screen.dart';

abstract class AppRoutes {
  static const inputPlate = '/';
  static const qrPayment = '/qr';
}

GoRouter buildAppRouter(BuildContext context) {
  return GoRouter(
    initialLocation: AppRoutes.inputPlate,
    routes: [
      GoRoute(
        path: AppRoutes.inputPlate,
        builder: (context, state) => const InputPlateScreen(),
      ),
      GoRoute(
        path: AppRoutes.qrPayment,
        builder: (context, state) => const QrPaymentScreen(),
      ),
    ],
  );
}
