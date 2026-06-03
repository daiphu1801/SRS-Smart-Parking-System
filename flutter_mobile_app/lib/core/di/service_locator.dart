import 'package:get_it/get_it.dart';
import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/core/utils/local_storage.dart';
import 'package:smart_parking_mobile/features/auth/repositories/auth_repository.dart';
import 'package:smart_parking_mobile/features/auth/services/auth_api_service.dart';
import 'package:smart_parking_mobile/features/customer_home/repositories/home_repository.dart';
import 'package:smart_parking_mobile/features/customer_home/services/home_api_service.dart';
import 'package:smart_parking_mobile/features/customer_profile/repositories/profile_repository.dart';
import 'package:smart_parking_mobile/features/customer_profile/services/profile_api_service.dart';
import 'package:smart_parking_mobile/features/customer_packages/repositories/booking_repository.dart';
import 'package:smart_parking_mobile/features/customer_packages/services/metadata_api_service.dart';
import 'package:smart_parking_mobile/features/customer_packages/services/operation_api_service.dart';
import 'package:smart_parking_mobile/features/customer_parking/repositories/parking_session_repository.dart';
import 'package:smart_parking_mobile/features/customer_parking/services/parking_session_api_service.dart';
import 'package:smart_parking_mobile/features/customer_complaint/services/complaint_api_service.dart';
import 'package:smart_parking_mobile/features/customer_complaint/repositories/complaint_repository.dart';
import 'package:smart_parking_mobile/features/customer_payment/services/payment_api_service.dart';
import 'package:smart_parking_mobile/features/customer_payment/repositories/payment_repository.dart';
import 'package:smart_parking_mobile/features/customer_history/services/history_api_service.dart';
import 'package:smart_parking_mobile/features/customer_history/repositories/history_repository.dart';
import 'package:smart_parking_mobile/features/customer_account/services/master_customer_api_service.dart';
import 'package:smart_parking_mobile/features/customer_notification/services/notification_api_service.dart';

/// Global service locator.
/// Usage: sl AuthRepository.
final sl = GetIt.instance;

/// Register all dependencies here. Called once from main().
void setupServiceLocator() {
  sl.registerLazySingleton<LocalStorage>(() => LocalStorage.instance);
  sl.registerLazySingleton<ApiClient>(
    () => ApiClient(storage: sl<LocalStorage>()),
  );
  sl.registerLazySingleton<AuthApiService>(
    () => AuthApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<AuthRepository>(
    () => AuthRepository(
      apiService: sl<AuthApiService>(),
      storage: sl<LocalStorage>(),
    ),
  );
  sl.registerLazySingleton<HomeApiService>(
    () => HomeApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<HomeRepository>(
    () => HomeRepository(apiService: sl<HomeApiService>()),
  );
  sl.registerLazySingleton<ProfileApiService>(
    () => ProfileApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<ProfileRepository>(
    () => ProfileRepository(sl<ProfileApiService>()),
  );
  sl.registerLazySingleton<MetadataApiService>(
    () => MetadataApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<OperationApiService>(
    () => OperationApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<BookingRepository>(
    () =>
        BookingRepository(sl<MetadataApiService>(), sl<OperationApiService>()),
  );
  sl.registerLazySingleton<ParkingSessionApiService>(
    () => ParkingSessionApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<ParkingSessionRepository>(
    () => ParkingSessionRepository(sl<ParkingSessionApiService>()),
  );
  sl.registerLazySingleton<ComplaintApiService>(
    () => ComplaintApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<ComplaintRepository>(
    () => ComplaintRepository(sl<ComplaintApiService>()),
  );
  sl.registerLazySingleton<PaymentApiService>(
    () => PaymentApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<PaymentRepository>(
    () => PaymentRepository(sl<PaymentApiService>()),
  );
  sl.registerLazySingleton<HistoryApiService>(
    () => HistoryApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<HistoryRepository>(
    () => HistoryRepository(sl<HistoryApiService>()),
  );
  sl.registerLazySingleton<MasterCustomerApiService>(
    () => MasterCustomerApiService(sl<ApiClient>()),
  );
  sl.registerLazySingleton<NotificationApiService>(
    () => NotificationApiService(sl<ApiClient>()),
  );
}
