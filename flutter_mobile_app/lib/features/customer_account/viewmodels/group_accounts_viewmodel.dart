import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import '../models/child_account.dart';

class GroupAccountsViewModel extends ChangeNotifier {
  ViewState<List<ChildAccount>> childrenState = const Idle();

  final List<ChildAccount> _mockChildren = [
    ChildAccount(
      id: 'C-1001',
      bookingId: 'BK-5001',
      fullName: 'Nguyễn Văn Bắc',
      address: 'Tầng 2, A101',
      phone: '0987654321',
    ),
  ];

  Future<void> fetchChildren(String bookingId) async {
    childrenState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 400));
      final filtered = _mockChildren.where((c) => c.bookingId == bookingId).toList();
      childrenState = Success(filtered);
    } catch (e) {
      childrenState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<ChildAccount> addChild({
    required String bookingId,
    required String fullName,
    String address = '',
    required String phone,
  }) async {
    await Future.delayed(const Duration(milliseconds: 500));
    final id = 'C-${DateTime.now().millisecondsSinceEpoch}';
    final child = ChildAccount(
      id: id,
      bookingId: bookingId,
      fullName: fullName,
      address: address,
      phone: phone,
    );
    _mockChildren.add(child);
    // refresh state
    childrenState = Success(_mockChildren.where((c) => c.bookingId == bookingId).toList());
    notifyListeners();
    return child;
  }

  Future<bool> removeChild(String childId) async {
    await Future.delayed(const Duration(milliseconds: 300));
    final idx = _mockChildren.indexWhere((c) => c.id == childId);
    if (idx == -1) return false;
    final bookingId = _mockChildren[idx].bookingId;
    _mockChildren.removeAt(idx);
    childrenState = Success(_mockChildren.where((c) => c.bookingId == bookingId).toList());
    notifyListeners();
    return true;
  }
}
