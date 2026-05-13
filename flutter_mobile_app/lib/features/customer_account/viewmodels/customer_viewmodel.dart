import 'package:flutter/foundation.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_account/models/customer_models.dart';

class CustomerViewModel extends ChangeNotifier {
  ViewState<Customer> customerState = const Idle();
  ViewState<GroupCustomer> groupState = const Idle();

  // Mock initial data
  Customer _mockCustomer = Customer(
    id: 'CUST-001',
    username: 'nguyenvana',
    fullName: 'Nguyen Van A',
    identityNumber: '012345678901',
    phone: '0901234567',
    address: '123 Main St, HCMC',
    email: 'nguyenvana@example.com',
    groupName: 'Group VIP',
  );

  GroupCustomer _mockGroup = GroupCustomer(
    id: 'GRP-100',
    name: 'Group VIP',
    ownerName: 'Nguyen Van A',
    ownerId: 'CUST-001',
    customers: [
      Customer(
        id: 'CUST-002',
        username: 'tranvanb',
        fullName: 'Tran Van B',
        identityNumber: '098765432109',
        phone: '0912345678',
        address: '456 Side St, HCMC',
        email: 'tranvanb@example.com',
        groupName: 'Group VIP',
      ),
    ],
  );

  /// Fetch Customer by ID
  Future<void> fetchCustomer(String customerId) async {
    customerState = const Loading();
    notifyListeners();

    // Mock API call
    await Future.delayed(const Duration(milliseconds: 500));

    // For mock purpose, we just return the mock customer regardless of ID
    // In real app, we would query the backend.
    customerState = Success(_mockCustomer);
    notifyListeners();
  }

  /// Update Customer
  Future<bool> updateCustomer(String customerId, Customer updatedData) async {
    customerState = const Loading();
    notifyListeners();

    // Mock API call
    await Future.delayed(const Duration(milliseconds: 500));

    // Update the mock data
    _mockCustomer = updatedData;
    customerState = Success(_mockCustomer);
    notifyListeners();
    return true; // Return success
  }

  /// Fetch Group Customer by Customer ID
  Future<void> fetchGroupCustomer(String customerId) async {
    groupState = const Loading();
    notifyListeners();

    // Mock API call
    await Future.delayed(const Duration(milliseconds: 500));

    // In a real scenario, this fetches the group that the customer belongs to.
    groupState = Success(_mockGroup);
    notifyListeners();
  }

  /// Update Group Customer List (For Owner)
  Future<bool> updateGroupCustomer(String groupId, List<Customer> updatedList) async {
    groupState = const Loading();
    notifyListeners();

    // Mock API call
    await Future.delayed(const Duration(milliseconds: 500));

    // Update the mock group's customer list
    _mockGroup = _mockGroup.copyWith(customers: updatedList);
    groupState = Success(_mockGroup);
    notifyListeners();
    return true; // Return success
  }
}
