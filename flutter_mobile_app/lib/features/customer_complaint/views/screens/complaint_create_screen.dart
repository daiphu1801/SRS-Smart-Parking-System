import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_complaint/viewmodels/complaint_viewmodel.dart';

class ComplaintCreateScreen extends StatefulWidget {
  const ComplaintCreateScreen({super.key});

  @override
  State<ComplaintCreateScreen> createState() => _ComplaintCreateScreenState();
}

class _ComplaintCreateScreenState extends State<ComplaintCreateScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descController = TextEditingController();
  bool _isSubmitting = false;

  @override
  void dispose() {
    _titleController.dispose();
    _descController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSubmitting = true);

    final success = await context.read<ComplaintViewModel>().createComplaint(
          customerId: 'CUST-001', // Mock ID
          title: _titleController.text.trim(),
          description: _descController.text.trim(),
          // Image upload skipped in mock UI
        );

    if (!mounted) return;
    setState(() => _isSubmitting = false);

    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Gửi khiếu nại thành công')),
      );
      context.pop();
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Có lỗi xảy ra, vui lòng thử lại')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Gửi Khiếu nại mới'),
      ),
      body: Form(
        key: _formKey,
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(AppTheme.pagePadding),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                'Chúng tôi luôn lắng nghe ý kiến của bạn để cải thiện chất lượng dịch vụ.',
                style: AppTheme.body.copyWith(color: AppTheme.subtle),
              ),
              const SizedBox(height: 24),
              
              AppTextField(
                label: 'Tiêu đề',
                placeholder: 'VD: Lỗi nhận diện biển số cổng A',
                controller: _titleController,
                validator: (val) {
                  if (val == null || val.trim().isEmpty) {
                    return 'Vui lòng nhập tiêu đề';
                  }
                  return null;
                },
              ),

              const SizedBox(height: 20),

              AppTextField(
                label: 'Nội dung phản ánh',
                placeholder: 'Mô tả chi tiết vấn đề bạn gặp phải...',
                controller: _descController,
                maxLines: 5,
                validator: (val) {
                  if (val == null || val.trim().isEmpty) {
                    return 'Vui lòng nhập nội dung';
                  }
                  return null;
                },
              ),

              const SizedBox(height: 24),

              Text('Hình ảnh đính kèm (nếu có)', style: AppTheme.heading3.copyWith(fontSize: 16)),
              const SizedBox(height: 12),
              InkWell(
                onTap: () {
                  // Simulate image picking
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Chức năng chọn ảnh đang được phát triển')),
                  );
                },
                borderRadius: BorderRadius.circular(AppTheme.radiusCard),
                child: Container(
                  height: 120,
                  decoration: BoxDecoration(
                    color: AppTheme.surface,
                    borderRadius: BorderRadius.circular(AppTheme.radiusCard),
                    border: Border.all(color: AppTheme.border, style: BorderStyle.solid),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.add_a_photo_outlined, size: 32, color: AppTheme.subtle),
                      const SizedBox(height: 8),
                      Text('Nhấn để tải ảnh lên',
                          style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 32),

              AppFilledButton(
                label: 'Gửi khiếu nại',
                isLoading: _isSubmitting,
                onPressed: _submit,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
