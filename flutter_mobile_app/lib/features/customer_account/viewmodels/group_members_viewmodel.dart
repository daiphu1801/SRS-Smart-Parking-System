import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_account/models/group_member.dart';
import 'package:smart_parking_mobile/features/customer_account/services/master_customer_api_service.dart';

class GroupMembersViewModel extends ChangeNotifier {
  final MasterCustomerApiService _apiService;

  GroupMembersViewModel(this._apiService);

  ViewState<List<GroupMember>> membersState = const Idle();

  Future<void> fetchMembers() async {
    membersState = const Loading();
    notifyListeners();

    try {
      final members = await _apiService.getCustomers(page: 0, size: 50);
      membersState = Success(members);
    } catch (e) {
      membersState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<GroupMember?> addMember({
    required String fullName,
    required String phone,
    String address = '',
  }) async {
    try {
      final newMember = await _apiService.createCustomer(
        fullName: fullName,
        phone: phone,
        address: address,
      );
      
      if (membersState is Success<List<GroupMember>>) {
        final currentList = (membersState as Success<List<GroupMember>>).data;
        membersState = Success([...currentList, newMember]);
        notifyListeners();
      } else {
        await fetchMembers();
      }
      return newMember;
    } catch (e) {
      // Could handle error via throwing or returning null and showing snackbar
      rethrow;
    }
  }

  Future<bool> removeMember(int childId) async {
    try {
      await _apiService.deleteCustomer(childId);
      if (membersState is Success<List<GroupMember>>) {
        final currentList = (membersState as Success<List<GroupMember>>).data;
        membersState = Success(currentList.where((m) => m.id != childId).toList());
        notifyListeners();
      }
      return true;
    } catch (e) {
      rethrow;
    }
  }
}
