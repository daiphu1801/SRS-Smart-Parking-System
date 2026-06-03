import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/features/customer_notification/models/notification_models.dart';

class NotificationApiService {
  final ApiClient _apiClient;

  NotificationApiService(this._apiClient);

  Future<List<AppNotification>> getNotifications() async {
    final response = await _apiClient.get(
      '/api/v1/notifications',
      authenticated: true,
    );
    
    final data = response.data as List<dynamic>? ?? [];
    return data.map((e) => AppNotification.fromJson(e)).toList();
  }
}
