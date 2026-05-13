import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';

class BookingViewModel extends ChangeNotifier {
  ViewState<List<Booking>> bookingsState = const Idle();
  ViewState<Booking> currentBookingState = const Idle();
  ViewState<List<BookingDetail>> bookingDetailsState = const Idle();
  ViewState<BookingDetail> currentBookingDetailState = const Idle();
  ViewState<List<PackagePrice>> packagePricesState = const Idle();

  // ── MOCK DATA (1 account = 1 booking, many booking details) ──────────────
  final List<PackagePrice> _mockPackagePrices = [
    PackagePrice(
      id: 'PP-01',
      packageId: 'PKG-01',
      packageName: 'Gói Tháng - Ô tô',
      vehicleType: 'CAR',
      durationDays: 30,
      price: 1500000,
    ),
    PackagePrice(
      id: 'PP-02',
      packageId: 'PKG-02',
      packageName: 'Gói Tháng - Xe máy',
      vehicleType: 'BIKE',
      durationDays: 30,
      price: 500000,
    ),
    PackagePrice(
      id: 'PP-03',
      packageId: 'PKG-03',
      packageName: 'Gói Quý - Ô tô',
      vehicleType: 'CAR',
      durationDays: 90,
      price: 4000000,
    ),
  ];

  final List<BookingDetail> _mockDetails = [
    BookingDetail(
      id: 'BD-1001',
      bookingId: 'BK-5001',
      customerId: 'CUST-001',
      plateNumber: '30A-123.45',
      vehicleType: 'CAR',
      packageType: 'Gói Tháng - Ô tô',
      duration: '30 ngày',
      price: 1500000,
      startDate: DateTime.now().subtract(const Duration(days: 15)),
      endDate: DateTime.now().add(const Duration(days: 15)),
      status: BookingStatus.active,
      packagePriceId: 'PP-01',
    ),
    BookingDetail(
      id: 'BD-1002',
      bookingId: 'BK-5001',
      customerId: 'CUST-001', // Thay đổi để test gia hạn gói cước
      plateNumber: '29B-987.65',
      vehicleType: 'CAR',
      packageType: 'Gói Tháng - Ô tô',
      duration: '30 ngày',
      price: 1500000,
      startDate: DateTime.now().subtract(const Duration(days: 40)),
      endDate: DateTime.now().subtract(const Duration(days: 10)),
      status: BookingStatus.expired,
      packagePriceId: 'PP-01',
    ),
    BookingDetail(
      id: 'BD-1003',
      bookingId: 'BK-5002',
      customerId: 'CUST-001',
      plateNumber: '51G-456.78',
      vehicleType: 'BIKE',
      packageType: 'Gói Tháng - Xe máy',
      duration: '30 ngày',
      price: 500000,
      startDate: DateTime.now().subtract(const Duration(days: 5)),
      endDate: DateTime.now().add(const Duration(days: 25)),
      status: BookingStatus.active,
      packagePriceId: 'PP-02',
    ),
  ];

  late final List<Booking> _mockBookings;

  BookingViewModel() {
    _mockBookings = [
      Booking(
        id: 'BK-5001',
        groupId: 'GRP-001',
        groupName: 'Căn hộ A101 - Nguyễn Văn A',
        totalVehicles: 2,
        duration: '30 ngày',
        paymentStatus: PaymentStatus.success,
        paymentMethod: PaymentMethod.vnpay,
        createdAt: DateTime.now().subtract(const Duration(days: 40)),
        totalAmount: 3000000,
        details: _mockDetails.where((d) => d.bookingId == 'BK-5001').toList(),
      ),
    ];
  }

  Future<void> fetchBookings(String customerId) async {
    bookingsState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 600));
      final activeBooking = _mockBookings.first;
      bookingsState = Success([activeBooking.copyWith(details: _mockDetails.where((d) => d.bookingId == activeBooking.id).toList())]);
    } catch (e) {
      bookingsState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchBookingById(String bookingId) async {
    currentBookingState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 500));
      final booking = _mockBookings.firstWhere(
        (b) => b.id == bookingId,
        orElse: () => throw Exception('Không tìm thấy hóa đơn'),
      );
      currentBookingState = Success(booking);
    } catch (e) {
      currentBookingState = Failure(e.toString());
    }
    notifyListeners();
  }

  /// Adds a vehicle to a booking and assigns it to a specific customer (child account)
  Future<BookingDetail?> addVehicleToBookingForCustomer({
    required String bookingId,
    required String customerId,
    required String plateNumber,
    required String vehicleType,
    required String packagePriceId,
  }) async {
    try {
      await Future.delayed(const Duration(milliseconds: 700));

      final bookingIndex = _mockBookings.indexWhere((b) => b.id == bookingId);
      if (bookingIndex == -1) {
        throw Exception('Không tìm thấy booking');
      }

      final booking = _mockBookings[bookingIndex];
      
      // Check limit
      if (booking.details.length >= booking.totalVehicles) {
        throw Exception('Số lượng xe đã đạt giới hạn tối đa của hợp đồng (${booking.totalVehicles} xe)');
      }

      final normalizedPlate = plateNumber.trim().toUpperCase();
      if (normalizedPlate.isEmpty) {
        throw Exception('Biển số xe không được để trống');
      }

      final duplicated = booking.details.any(
        (detail) => detail.plateNumber.toUpperCase() == normalizedPlate,
      );
      if (duplicated) {
        throw Exception('Biển số xe này đã tồn tại trong booking');
      }

      final pkgPrice = _mockPackagePrices.firstWhere((p) => p.id == packagePriceId);
      final now = DateTime.now();

      final newDetail = BookingDetail(
        id: 'BD-${now.millisecondsSinceEpoch}',
        bookingId: booking.id,
        customerId: customerId,
        plateNumber: normalizedPlate,
        vehicleType: vehicleType,
        packageType: pkgPrice.packageName,
        duration: '${pkgPrice.durationDays} ngày',
        price: pkgPrice.price,
        startDate: now,
        endDate: now.add(Duration(days: pkgPrice.durationDays)),
        status: BookingStatus.active,
        packagePriceId: pkgPrice.id,
      );

      final updatedDetails = [...booking.details, newDetail];
      final updatedBooking = booking.copyWith(
        // We don't increment totalVehicles if it's a fixed contract limit
        // But here totalVehicles seems to represent the limit in mock
        // totalVehicles: updatedDetails.length, 
        totalAmount: booking.totalAmount + newDetail.price,
        details: updatedDetails,
      );

      _mockBookings[bookingIndex] = updatedBooking;

      if (currentBookingState case Success<Booking> currentBooking
          when currentBooking.data.id == bookingId) {
        currentBookingState = Success(updatedBooking);
      }

      if (bookingsState case Success<List<Booking>> currentBookings) {
        bookingsState = Success(
          currentBookings.data
              .map((item) => item.id == bookingId ? updatedBooking : item)
              .toList(),
        );
      }

      notifyListeners();
      return newDetail;
    } catch (e) {
      return null;
    }
  }

  Future<void> fetchBookingDetails(String customerId) async {
    bookingDetailsState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 600));
      final filtered = _mockDetails
          .where((d) => d.customerId == customerId)
          .toList();
      bookingDetailsState = Success(filtered);
    } catch (e) {
      bookingDetailsState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchBookingDetailById(String detailId) async {
    currentBookingDetailState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 500));
      final detail = _mockDetails.firstWhere(
        (d) => d.id == detailId,
        orElse: () => throw Exception('Không tìm thấy chi tiết gói cước'),
      );
      currentBookingDetailState = Success(detail);
    } catch (e) {
      currentBookingDetailState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<bool> renewBookingDetail(
      BookingDetail oldDetail, int newDurationMonths) async {
    try {
      await Future.delayed(const Duration(seconds: 1));

      final newDetail = BookingDetail(
        id: 'BD-${DateTime.now().millisecondsSinceEpoch}',
        bookingId: oldDetail.bookingId,
        customerId: oldDetail.customerId,
        plateNumber: oldDetail.plateNumber,
        vehicleType: oldDetail.vehicleType,
        packageType: oldDetail.packageType,
        duration: '$newDurationMonths tháng',
        price: oldDetail.price * newDurationMonths,
        startDate: DateTime.now(),
        endDate: DateTime.now().add(Duration(days: 30 * newDurationMonths)),
        status: BookingStatus.active,
        packagePriceId: oldDetail.packagePriceId,
      );

      _mockDetails.insert(0, newDetail);

      final bookingIndex = _mockBookings.indexWhere((b) => b.id == oldDetail.bookingId);
      if (bookingIndex != -1) {
        final booking = _mockBookings[bookingIndex];
        final updatedDetails = [
          newDetail,
          ...booking.details.where((detail) => detail.id != oldDetail.id),
        ];
        _mockBookings[bookingIndex] = booking.copyWith(
          totalVehicles: updatedDetails.length,
          totalAmount: booking.totalAmount - oldDetail.price + newDetail.price,
          details: updatedDetails,
        );
      }

      await fetchBookings(oldDetail.customerId);
      return true;
    } catch (e) {
      return false;
    }
  }

  Future<void> fetchPackagePrices() async {
    packagePricesState = const Loading();
    notifyListeners();
    try {
      await Future.delayed(const Duration(milliseconds: 400));
      packagePricesState = Success(_mockPackagePrices);
    } catch (e) {
      packagePricesState = Failure(e.toString());
    }
    notifyListeners();
  }
}
