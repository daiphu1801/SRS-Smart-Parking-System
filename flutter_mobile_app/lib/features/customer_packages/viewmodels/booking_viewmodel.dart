import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/features/customer_packages/repositories/booking_repository.dart';

class BookingViewModel extends ChangeNotifier {
  final BookingRepository _repository;

  ViewState<BookingAndDetailResponse> bookingDataState = const Idle();
  ViewState<List<BookingDetailDto>> draftsState = const Idle();
  ViewState<List<Booking>> bookingsState = const Idle();
  ViewState<Booking> currentBookingState = const Idle();
  ViewState<BookingDetail> currentBookingDetailState = const Idle();
  ViewState<List<BookingDetail>> filteredBookingDetailsState = const Idle();

  BookingViewModel(this._repository);

  Future<void> fetchBookingAndDetails() async {
    bookingDataState = const Loading();
    notifyListeners();

    try {
      final data = await _repository.getBookingAndDetails();
      bookingDataState = Success(data);
    } catch (e) {
      bookingDataState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchBookings(String customerId) async {
    bookingsState = const Loading();
    notifyListeners();

    try {
      final data = await _repository.getBookingAndDetails();
      bookingDataState = Success(data);
      final booking = Booking.fromBackend(data);
      bookingsState = Success(booking.id == '0' ? <Booking>[] : [booking]);
    } catch (e) {
      bookingsState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchBookingById(String bookingId) async {
    currentBookingState = const Loading();
    notifyListeners();

    try {
      final data = await _repository.getBookingAndDetails();
      bookingDataState = Success(data);
      final booking = Booking.fromBackend(data);
      currentBookingState = Success(booking);
    } catch (e) {
      currentBookingState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchBookingDetailById(String bookingDetailId) async {
    currentBookingDetailState = const Loading();
    notifyListeners();

    try {
      final data = await _repository.getBookingAndDetails();
      bookingDataState = Success(data);
      final booking = Booking.fromBackend(data);
      final detail = booking.details.firstWhere(
        (item) => item.id == bookingDetailId,
        orElse: () => throw Exception('Không tìm thấy phương tiện này.'),
      );
      currentBookingDetailState = Success(detail);
    } catch (e) {
      currentBookingDetailState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchFilteredBookingDetails([List<String>? statuses]) async {
    filteredBookingDetailsState = const Loading();
    notifyListeners();

    try {
      final data = await _repository.getBookingDetails(statuses: statuses);
      final details = data.map((dto) => BookingDetail.fromDto(dto)).toList();
      filteredBookingDetailsState = Success(details);
    } catch (e) {
      filteredBookingDetailsState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<bool> renewBookingDetail(BookingDetail oldDetail, int newPackagePriceId) async {
    try {
      final oldId = int.tryParse(oldDetail.id) ?? 0;
      await _repository.renewBooking(oldId, newPackagePriceId);
      // Giả sử tạo draft gia hạn thành công. Refresh lại giỏ hàng (drafts)
      await fetchDrafts();
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<void> fetchDrafts() async {
    draftsState = const Loading();
    notifyListeners();

    try {
      final drafts = await _repository.getDrafts();
      draftsState = Success(drafts);
    } catch (e) {
      draftsState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> deleteSelectedDrafts(List<String> ids) async {
    try {
      final intIds = ids.map((e) => int.tryParse(e) ?? 0).toList();
      await _repository.deleteSelectedDrafts(intIds);
      // Refresh the drafts and the filtered details state
      await fetchDrafts();
    } catch (e) {
      // Throw so the UI can catch it
      rethrow;
    }
  }

  // Load both simultaneously
  Future<void> initializeData() async {
    bookingDataState = const Loading();
    draftsState = const Loading();
    notifyListeners();

    try {
      final data = await _repository.getBookingAndDetails();
      final drafts = await _repository.getDrafts();
      bookingDataState = Success(data);
      bookingsState = Success([Booking.fromBackend(data)]);
      draftsState = Success(drafts);
    } catch (e) {
      bookingDataState = Failure(e.toString());
      bookingsState = Failure(e.toString());
      draftsState = Failure(e.toString());
    }
    notifyListeners();
  }
}
