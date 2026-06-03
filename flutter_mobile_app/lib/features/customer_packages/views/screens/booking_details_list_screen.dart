import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_packages/viewmodels/booking_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_packages/views/widgets/package_widgets.dart';

class BookingDetailsListScreen extends StatefulWidget {
  final String? status;
  final String title;

  const BookingDetailsListScreen({
    super.key,
    this.status,
    required this.title,
  });

  @override
  State<BookingDetailsListScreen> createState() => _BookingDetailsListScreenState();
}

class _BookingDetailsListScreenState extends State<BookingDetailsListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final statuses = widget.status != null ? [widget.status!] : null;
      context.read<BookingViewModel>().fetchFilteredBookingDetails(statuses);
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
        title: Text(widget.title),
      ),
      body: Consumer<BookingViewModel>(
        builder: (context, vm, child) {
          final state = vm.filteredBookingDetailsState;

          if (state is Loading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is Failure) {
            return AppEmptyState(
              icon: Icons.error_outline,
              title: 'Lỗi tải dữ liệu',
              subtitle: (state as Failure).message,
            );
          }

          if (state is Success) {
            final details = (state as Success).data as List;
            if (details.isEmpty) {
              return const AppEmptyState(
                icon: Icons.directions_car_outlined,
                title: 'Không có dữ liệu',
                subtitle: 'Hiện tại chưa có phương tiện nào trong danh sách này.',
              );
            }

            return RefreshIndicator(
              color: AppTheme.primary,
              onRefresh: () async {
                final statuses = widget.status != null ? [widget.status!] : null;
                return vm.fetchFilteredBookingDetails(statuses);
              },
              child: ListView.separated(
                padding: const EdgeInsets.all(AppTheme.pagePadding),
                itemCount: details.length,
                separatorBuilder: (context, index) => const SizedBox(height: 12),
                itemBuilder: (context, index) {
                  final detail = details[index];
                  return BookingDetailCard(
                    detail: detail,
                    hideActions: true,
                  );
                },
              ),
            );
          }

          return const SizedBox.shrink();
        },
      ),
    );
  }
}
