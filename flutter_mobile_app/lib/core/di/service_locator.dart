import 'package:get_it/get_it.dart';
import 'package:smart_parking_mobile/core/utils/local_storage.dart';
import 'package:smart_parking_mobile/features/auth/services/mock_auth_service.dart';

/// Global service locator.
/// Usage:  sl<MockAuthService>()
final sl = GetIt.instance;

/// Register all dependencies here. Called once from main().
void setupServiceLocator() {
  // ── Core ────────────────────────────────────────────────────────────────
  sl.registerLazySingleton<LocalStorage>(() => LocalStorage.instance);

  // ── Services (Mock — swap to real ApiService later) ─────────────────────
  // When backend is ready: replace MockAuthService with AuthService(ApiClient())
  sl.registerLazySingleton<MockAuthService>(() => MockAuthService());
}
