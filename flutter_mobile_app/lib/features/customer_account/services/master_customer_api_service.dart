import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/features/customer_account/models/group_member.dart';

class MasterCustomerApiService {
  final ApiClient _apiClient;

  MasterCustomerApiService(this._apiClient);

  Future<List<GroupMember>> getCustomers({int page = 0, int size = 20}) async {
    final response = await _apiClient.get(
      '/api/v1/master/customer',
      queryParameters: {
        'page': page.toString(),
        'size': size.toString(),
      },
      authenticated: true,
    );

    if (response.success && response.data != null) {
      final content = response.data['content'] as List;
      return content.map((json) => GroupMember.fromJson(json)).toList();
    } else {
      throw Exception(response.message ?? 'Failed to fetch group members');
    }
  }

  Future<GroupMember> createCustomer({
    required String fullName,
    required String phone,
    String address = '',
  }) async {
    final response = await _apiClient.post(
      '/api/v1/master/customer',
      data: {
        'fullName': fullName,
        'phone': phone,
        'address': address,
        // Using roleId 3 as typical customer or leaving it out if backend defaults it
        'roleId': 3, 
      },
      authenticated: true,
    );

    if (response.success && response.data != null) {
      return GroupMember.fromJson(response.data);
    } else {
      throw Exception(response.message ?? 'Failed to create group member');
    }
  }

  Future<GroupMember> updateCustomer({
    required int id,
    required String fullName,
  }) async {
    final response = await _apiClient.put(
      '/api/v1/master/customer/$id',
      body: {
        'fullName': fullName,
      },
      authenticated: true,
    );

    if (response.success && response.data != null) {
      return GroupMember.fromJson(response.data);
    } else {
      throw Exception(response.message ?? 'Failed to update group member');
    }
  }

  Future<void> deleteCustomer(int id) async {
    final response = await _apiClient.delete(
      '/api/v1/master/customer/$id',
      authenticated: true,
    );

    if (!response.success) {
      throw Exception(response.message ?? 'Failed to delete group member');
    }
  }
}
