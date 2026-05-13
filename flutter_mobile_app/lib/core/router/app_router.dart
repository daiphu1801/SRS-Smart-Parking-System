import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/login_flow_screen.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/register_screen.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/forgot_password_screen.dart';
import 'package:smart_parking_mobile/features/customer_home/views/screens/customer_home_screen.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/screens/booking_list_screen.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/screens/booking_detail_screen.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/screens/booking_detail_item_screen.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/screens/renew_package_screen.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/screens/add_vehicle_screen.dart';
import 'package:smart_parking_mobile/features/customer_history/views/screens/history_screen.dart';
import 'package:smart_parking_mobile/features/app_shell/app_shell.dart';

// New Screens
import 'package:smart_parking_mobile/features/customer_account/views/screens/account_menu_screen.dart';
import 'package:smart_parking_mobile/features/customer_account/views/screens/customer_detail_screen.dart';
import 'package:smart_parking_mobile/features/customer_account/views/screens/customer_edit_screen.dart';
import 'package:smart_parking_mobile/features/customer_account/views/screens/group_detail_screen.dart';
import 'package:smart_parking_mobile/features/customer_account/views/screens/group_edit_screen.dart';
import 'package:smart_parking_mobile/features/customer_account/views/screens/child_accounts_screen.dart';
import 'package:smart_parking_mobile/features/customer_parking/views/screens/parking_session_list_screen.dart';
import 'package:smart_parking_mobile/features/customer_parking/views/screens/parking_session_detail_screen.dart';
import 'package:smart_parking_mobile/features/customer_complaint/views/screens/complaint_list_screen.dart';
import 'package:smart_parking_mobile/features/customer_complaint/views/screens/complaint_detail_screen.dart';
import 'package:smart_parking_mobile/features/customer_complaint/views/screens/complaint_create_screen.dart';
import 'package:smart_parking_mobile/features/customer_payment/views/screens/qr_payment_screen.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/change_password_screen.dart';
import 'package:smart_parking_mobile/features/customer_notification/views/screens/notification_list_screen.dart';

/// Route name constants — use these instead of raw strings.

abstract class AppRoutes {
  static const loginPhone = '/login';
  static const register = '/register';
  static const forgotPassword = '/forgot-password';
  static const customerShell = '/customer';
  static const customerHome = '/customer/home';
  static const customerPackages = '/customer/packages';
  static const customerHistory = '/customer/history';
  static const accountMenu = '/customer/account';
  static const complaintList = '/customer/complaints';
  static const complaintCreate = '/customer/complaints/create';
  static const complaintDetail = '/customer/complaints/:id';
  static const customerDetail = '/customer/detail/:id';
  static const customerEdit = '/customer/edit/:id';
  static const groupDetail = '/customer/group/:id';
  static const groupEdit = '/customer/group/edit/:id';
  static const childAccounts = '/customer/child-accounts';
  // Old buy-package removed
  static const bookingDetail = '/customer/bookings/:id';
  static const bookingDetailItem = '/customer/booking-detail/:id';
  static const renewPackage = '/customer/booking-detail/renew/:id';
  static const addVehicle = '/customer/bookings/:id/add-vehicle';
  // Parking sessions
  static const parkingSessionList = '/customer/parking';
  static const parkingSessionDetail = '/customer/parking/:id';
  // Auth extras
  static const changePassword = '/customer/change-password';
  // Notifications
  static const notificationList = '/customer/notifications';
}

/// Builds and returns the app's GoRouter instance.
/// Redirect logic lives here — no auth checks scattered in widgets.
GoRouter buildAppRouter(BuildContext context, AuthViewModel authViewModel) {
  return GoRouter(
    initialLocation: AppRoutes.customerHome,
    refreshListenable: authViewModel,
    redirect: (context, state) async {
      final isLoggedIn = authViewModel.isAuthenticated;
      final isAuthRoute = state.matchedLocation == AppRoutes.loginPhone ||
          state.matchedLocation == AppRoutes.register ||
          state.matchedLocation == AppRoutes.forgotPassword;

      if (!isLoggedIn && !isAuthRoute) return AppRoutes.loginPhone;
      if (isLoggedIn && isAuthRoute) return AppRoutes.customerHome;
      return null;
    },
    routes: [
      // ── Auth Routes ────────────────────────────────────────────────────────
      // 3-step login flow: phone → OTP → password (managed by LoginFlowScreen)
      GoRoute(path: AppRoutes.loginPhone, builder: (context, state) => const LoginFlowScreen()),
      
      GoRoute(path: AppRoutes.register, builder: (context, state) => const RegisterScreen()),
      GoRoute(path: AppRoutes.forgotPassword, builder: (context, state) => const ForgotPasswordScreen()),

      // ── Customer Shell (Bottom Nav) ────────────────────────────────────────
      ShellRoute(
        builder: (context, state, child) => AppShell(child: child),
        routes: [
          GoRoute(
            path: AppRoutes.customerHome,
            pageBuilder: (context, state) => const NoTransitionPage(child: CustomerHomeScreen()),
          ),
          GoRoute(
            path: AppRoutes.customerPackages,
            pageBuilder: (context, state) => const NoTransitionPage(child: BookingListScreen()),
          ),
          GoRoute(
            path: AppRoutes.customerHistory,
            pageBuilder: (context, state) => const NoTransitionPage(child: CustomerHistoryScreen()),
          ),
          GoRoute(
            path: AppRoutes.accountMenu,
            pageBuilder: (context, state) => const NoTransitionPage(child: AccountMenuScreen()),
          ),
          GoRoute(
            path: AppRoutes.parkingSessionList,
            pageBuilder: (context, state) => const NoTransitionPage(child: ParkingSessionListScreen()),
          ),
        ],
      ),

      // ── Customer Profile & Group Routes ────────────────────────────────────
      GoRoute(
        path: AppRoutes.customerDetail,
        builder: (context, state) => CustomerDetailScreen(customerId: (state.pathParameters ?? {})['id'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.customerEdit,
        builder: (context, state) => CustomerEditScreen(customerId: (state.pathParameters ?? {})['id'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.groupDetail,
        builder: (context, state) => GroupDetailScreen(customerId: (state.pathParameters ?? {})['id'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.groupEdit,
        builder: (context, state) => GroupEditScreen(groupId: (state.pathParameters ?? {})['id'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.childAccounts,
        builder: (context, state) => const ChildAccountsScreen(),
      ),
      GoRoute(
        path: AppRoutes.bookingDetail,
        builder: (context, state) => BookingDetailScreen(bookingId: (state.pathParameters ?? {})['id'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.bookingDetailItem,
        builder: (context, state) => BookingDetailItemScreen(bookingDetailId: (state.pathParameters ?? {})['id'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.renewPackage,
        builder: (context, state) => RenewPackageScreen(bookingDetailId: (state.pathParameters ?? {})['id'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.addVehicle,
        builder: (context, state) {
          final bookingId = (state.pathParameters ?? {})['id'] ?? '';
          return AddVehicleScreen(bookingId: bookingId);
        },
      ),
      GoRoute(
        path: AppRoutes.parkingSessionDetail,
        builder: (context, state) => ParkingSessionDetailScreen(sessionId: (state.pathParameters ?? {})['id'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.complaintList,
        builder: (context, state) => const ComplaintListScreen(),
      ),
      GoRoute(
        path: AppRoutes.complaintCreate,
        builder: (context, state) => const ComplaintCreateScreen(),
      ),
      GoRoute(
        path: AppRoutes.complaintDetail,
        builder: (context, state) => ComplaintDetailScreen(complaintId: (state.pathParameters ?? {})['id'] ?? ''),
      ),
      GoRoute(
        path: '/payment/qr',
        builder: (context, state) {
          final extra = state.extra as Map<String, dynamic>? ?? {};
          return QRPaymentScreen(
            amount: extra['amount'] as double? ?? 0.0,
            targetId: extra['targetId'] as String? ?? '',
            isSession: extra['isSession'] as bool? ?? false,
          );
        },
      ),
      GoRoute(
        path: AppRoutes.changePassword,
        builder: (context, state) => const ChangePasswordScreen(),
      ),
      GoRoute(
        path: AppRoutes.notificationList,
        builder: (context, state) => const NotificationListScreen(),
      ),
    ],
  );
}
