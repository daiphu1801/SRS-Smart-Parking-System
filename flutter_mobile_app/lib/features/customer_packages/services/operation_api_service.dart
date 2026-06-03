import '../../../core/network/api_client.dart';
import '../models/booking_models.dart';

class OperationApiService {
  final ApiClient _apiClient;

  OperationApiService(this._apiClient);

  Future<BookingAndDetailResponse> getBookingAndDetails() async {
    final response = await _apiClient.get(
      '/api/v1/customer/operation/booking',
      authenticated: true,
    );

    final data = response.data as Map<String, dynamic>;
    return BookingAndDetailResponse.fromJson(data);
  }

  Future<List<BookingDetailDto>> getBookingDetails({
    List<String>? statuses,
  }) async {
    final Map<String, String> queryParams = {};
    if (statuses != null && statuses.isNotEmpty) {
      // Spring usually accepts array parameters as repeated keys or comma-separated string
      queryParams['statuses'] = statuses.join(',');
    }

    final response = await _apiClient.get(
      '/api/v1/customer/operation/booking-details',
      queryParameters: queryParams,
      authenticated: true,
    );

    final data = response.data as List<dynamic>? ?? [];
    return data.map((e) => BookingDetailDto.fromJson(e)).toList();
  }


  Future<List<BookingDetailDto>> getDrafts() async {
    final response = await _apiClient.get(
      '/api/v1/customer/operation/drafts',
      authenticated: true,
    );

    final data = response.data as List<dynamic>? ?? [];
    return data.map((e) => BookingDetailDto.fromJson(e)).toList();
  }

  Future<BookingDetailDto> createDraft(
    BookingDetailCreateRequest request,
  ) async {
    final response = await _apiClient.post(
      '/api/v1/customer/operation',
      data: request.toJson(),
      authenticated: true,
    );

    final data = response.data as Map<String, dynamic>;
    return BookingDetailDto.fromJson(data);
  }

  Future<void> deleteSelectedDrafts(List<int> draftIds) async {
    await _apiClient.delete(
      '/api/v1/customer/operation/drafts',
      body: {'draftIds': draftIds},
      authenticated: true,
    );
  }

  Future<List<BookingDetailDto>> renewBooking(int oldBookingDetailId, int newPackagePriceId) async {
    final response = await _apiClient.post(
      '/api/v1/customer/operation/renew_booking',
      data: {
        'items': [
          {
            'oldBookingDetailId': oldBookingDetailId,
            'newPackagePriceId': newPackagePriceId,
          }
        ]
      },
      authenticated: true,
    );

    final data = response.data as List<dynamic>? ?? [];
    return data.map((e) => BookingDetailDto.fromJson(e)).toList();
  }
}
