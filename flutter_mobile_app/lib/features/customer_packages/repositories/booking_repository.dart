import '../models/booking_models.dart';
import '../models/metadata_models.dart';
import '../services/metadata_api_service.dart';
import '../services/operation_api_service.dart';

class BookingRepository {
  final MetadataApiService _metadataService;
  final OperationApiService _operationService;

  BookingRepository(this._metadataService, this._operationService);

  // Metadata operations
  Future<List<AllowedVehicleType>> getAllowedVehicleTypes() async {
    return await _metadataService.getAllowedVehicleTypes();
  }

  Future<List<AvailablePackagePrice>> getAvailablePackages(
    int vehicleTypeId,
  ) async {
    return await _metadataService.getAvailablePackages(vehicleTypeId);
  }

  // Operation operations
  Future<BookingAndDetailResponse> getBookingAndDetails() async {
    return await _operationService.getBookingAndDetails();
  }

  Future<List<BookingDetailDto>> getBookingDetails({List<String>? statuses}) async {
    return await _operationService.getBookingDetails(statuses: statuses);
  }

  Future<List<BookingDetailDto>> getDrafts() async {
    return await _operationService.getDrafts();
  }

  Future<BookingDetailDto> createDraft(
    BookingDetailCreateRequest request,
  ) async {
    return await _operationService.createDraft(request);
  }

  Future<void> deleteSelectedDrafts(List<int> draftIds) async {
    return await _operationService.deleteSelectedDrafts(draftIds);
  }

  Future<List<BookingDetail>> renewBooking(int oldBookingDetailId, int newPackagePriceId) async {
    final dtos = await _operationService.renewBooking(oldBookingDetailId, newPackagePriceId);
    return dtos.map((dto) => BookingDetail.fromDto(dto)).toList();
  }
}
