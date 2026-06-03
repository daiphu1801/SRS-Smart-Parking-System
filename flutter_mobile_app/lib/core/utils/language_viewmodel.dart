import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/local_storage.dart';

class LanguageViewModel extends ChangeNotifier {
  final LocalStorage _storage;
  Locale _currentLocale = const Locale('vi'); // Default language is Vietnamese

  LanguageViewModel(this._storage) {
    _loadLocale();
  }

  Locale get currentLocale => _currentLocale;

  Future<void> _loadLocale() async {
    final savedLanguageCode = await _storage.getLocale();
    if (savedLanguageCode != null) {
      _currentLocale = Locale(savedLanguageCode);
      notifyListeners();
    }
  }

  Future<void> changeLanguage(Locale locale) async {
    if (_currentLocale == locale) return;
    
    _currentLocale = locale;
    await _storage.saveLocale(locale.languageCode);
    notifyListeners();
  }
}
