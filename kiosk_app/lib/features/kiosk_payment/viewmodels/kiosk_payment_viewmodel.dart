import 'dart:async';
import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/models/kiosk_payment_request.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/models/kiosk_payment_response.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/repositories/kiosk_payment_repository.dart';

class KioskPaymentViewModel extends ChangeNotifier {
  final KioskPaymentRepository _repository;

  KioskPaymentViewModel(this._repository);

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _errorMessage;
  String? get errorMessage => _errorMessage;

  KioskPaymentResponse? _paymentResponse;
  KioskPaymentResponse? get paymentResponse => _paymentResponse;

  Timer? _timeoutTimer;

  Future<bool> initiatePayment(String vehicleNo) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final request = PaymentSessionRequest(vehicleNo: vehicleNo);
      _paymentResponse = await _repository.initiatePaymentSession(request);
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  void startTimeoutTimer(VoidCallback onTimeout) {
    cancelTimer();
    // 5 minutes timeout
    _timeoutTimer = Timer(const Duration(minutes: 5), () {
      onTimeout();
    });
  }

  void cancelTimer() {
    _timeoutTimer?.cancel();
    _timeoutTimer = null;
  }

  void reset() {
    _paymentResponse = null;
    _errorMessage = null;
    cancelTimer();
    notifyListeners();
  }

  @override
  void dispose() {
    cancelTimer();
    super.dispose();
  }
}
