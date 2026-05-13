import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/widgets/package_widgets.dart';

class BookingListScreen extends StatefulWidget {
  const BookingListScreen({super.key});

  @override
  State<BookingListScreen> createState() => _BookingListScreenState();
}

class _BookingListScreenState extends State<BookingListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<BookingViewModel>().fetchBookings('CUST-001');
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: context.canPop()
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => context.pop(),
              )
            : null,
        title: const Text('Hợp đồng gói tháng'),
      ),
      body: Consumer<BookingViewModel>(
        builder: (context, vm, child) {
          return switch (vm.bookingsState) {
            Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: 'Lỗi tải dữ liệu',
                subtitle: msg,
              ),
            Success(data: var bookings) when bookings.isEmpty => const AppEmptyState(
                icon: Icons.receipt_long_outlined,
                title: 'Chưa có hợp đồng nào',
                subtitle: 'Gói gửi xe tháng sẽ giúp bạn tiết kiệm chi phí gửi xe đáng kể.',
              ),
            Success(data: var bookings) => RefreshIndicator(
                color: AppTheme.primary,
                onRefresh: () => vm.fetchBookings('CUST-001'),
                child: ListView(
                  padding: const EdgeInsets.all(AppTheme.pagePadding),
                  children: [
                    BookingOverviewHeader(booking: bookings.first),
                    const SizedBox(height: AppTheme.sectionGap),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('Danh sách phương tiện', style: AppTheme.heading3),
                        TextButton.icon(
                          onPressed: () => context.push('/customer/bookings/${bookings.first.id}/add-vehicle'),
                          icon: const Icon(Icons.add, size: 18),
                          label: const Text('Thêm xe'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    if (bookings.first.details.isEmpty)
                      const AppEmptyState(
                        icon: Icons.directions_car_outlined,
                        title: 'Chưa có phương tiện',
                        subtitle: 'Nhấn "Thêm xe" để bắt đầu đăng ký gói tháng.',
                      )
                    else
                      ListView.separated(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        itemCount: bookings.first.details.length,
                        separatorBuilder: (context, index) => const SizedBox(height: 12),
                        itemBuilder: (context, index) {
                          return BookingDetailCard(detail: bookings.first.details[index]);
                        },
                      ),
                  ],
                ),
              ),
            _ => const SizedBox.shrink(),
          };
        },
      ),
    );
  }
}

