class AppConstants {
  AppConstants._();

  static const String appName = 'Smart Parking';

  // Route names
  static const String routeLogin = '/login';
  static const String routeOtp = '/otp';
  static const String routeCustomerHome = '/customer/home';
  static const String routeCustomerPackages = '/customer/packages';
  static const String routeCustomerHistory = '/customer/history';
  static const String routeCustomerComplaint = '/customer/complaint';
  static const String routeAddVehicle = '/customer/add-vehicle';
  static const String routeBuyPackage = '/customer/buy-package';


  // Shared Prefs keys
  static const String keyToken = 'auth_token';
  static const String keyPhone = 'user_phone';

  // Mobile chỉ có Customer
  static const String roleCustomer = 'CUSTOMER';
}
