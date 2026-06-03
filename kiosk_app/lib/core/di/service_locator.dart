import 'package:get_it/get_it.dart';
import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/core/utils/local_storage.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/repositories/kiosk_payment_repository.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/viewmodels/kiosk_payment_viewmodel.dart';

/// Global service locator.
final sl = GetIt.instance;

/// Register all dependencies here. Called once from main().
void setupServiceLocator() {
  sl.registerLazySingleton<LocalStorage>(() => LocalStorage.instance);
  sl.registerLazySingleton<ApiClient>(
    () => ApiClient(storage: sl<LocalStorage>()),
  );
  
  // Kiosk Payment
  sl.registerLazySingleton<KioskPaymentRepository>(
    () => KioskPaymentRepository(sl<ApiClient>()),
  );
  sl.registerLazySingleton<KioskPaymentViewModel>(
    () => KioskPaymentViewModel(sl<KioskPaymentRepository>()),
  );
}
