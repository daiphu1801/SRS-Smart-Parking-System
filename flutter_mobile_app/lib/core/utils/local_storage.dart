import 'package:shared_preferences/shared_preferences.dart';
import 'package:smart_parking_mobile/core/constants/app_constants.dart';

/// Abstraction over SharedPreferences for storing auth credentials.
/// Single source of truth for token read/write — swap backend later here only.
class LocalStorage {
  LocalStorage._();
  static LocalStorage? _instance;
  static LocalStorage get instance => _instance ??= LocalStorage._();

  // ── Token ──────────────────────────────────────────────────────────────────
  Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(AppConstants.keyToken, token);
  }

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(AppConstants.keyToken);
  }

  // ── Auth Meta ──────────────────────────────────────────────────────────────
  Future<void> saveAuthMeta({
    required String accountType,
    required int accountId,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('account_type', accountType);
    await prefs.setInt('account_id', accountId);
  }

  // ── Clear ──────────────────────────────────────────────────────────────────
  Future<void> clearAuth() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(AppConstants.keyToken);
    await prefs.remove('account_type');
    await prefs.remove('account_id');
  }

  Future<bool> isLoggedIn() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }
}
