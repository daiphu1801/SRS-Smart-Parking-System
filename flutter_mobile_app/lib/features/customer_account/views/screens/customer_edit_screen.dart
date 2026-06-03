import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_profile/viewmodels/profile_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_profile/models/customer_profile_models.dart';

class CustomerEditScreen extends StatefulWidget {
  final String customerId;
  const CustomerEditScreen({super.key, required this.customerId});

  @override
  State<CustomerEditScreen> createState() => _CustomerEditScreenState();
}

class _CustomerEditScreenState extends State<CustomerEditScreen> {
  final _formKey = GlobalKey<FormState>();
  
  late TextEditingController _fullNameController;
  late TextEditingController _phoneController;
  late TextEditingController _addressController;
  
  bool _isLoading = false;
  bool _obscurePhone = true;

  @override
  void initState() {
    super.initState();
    _fullNameController = TextEditingController();
    _phoneController = TextEditingController();
    _addressController = TextEditingController();

    // Populate data from ViewModel
    final state = context.read<ProfileViewModel>().profileState;
    if (state is Success<CustomerProfile>) {
      final customer = state.data;
      _fullNameController.text = customer.fullName;
      _phoneController.text = customer.phone;
      _addressController.text = customer.address;
    }
  }

  @override
  void dispose() {
    _fullNameController.dispose();
    _phoneController.dispose();
    _addressController.dispose();
    super.dispose();
  }

  Future<void> _saveChanges() async {
    if (_fullNameController.text.trim().isEmpty || 
        _phoneController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng điền đầy đủ các trường bắt buộc')),
      );
      return;
    }
    
    setState(() => _isLoading = true);
    
    final viewModel = context.read<ProfileViewModel>();
    final currentState = viewModel.profileState;
    if (currentState is Success<CustomerProfile>) {
      final request = UpdateProfileRequest(
        fullName: _fullNameController.text.trim(),
        phone: _phoneController.text.trim(),
        address: _addressController.text.trim(),
      );
      
      final success = await viewModel.updateProfile(request);
      setState(() => _isLoading = false);
      
      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Cập nhật thông tin thành công!')),
        );
        context.pop();
      }
    } else {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = context.watch<ProfileViewModel>().profileState;
    
    // Ensure we have data
    if (state is! Success<CustomerProfile>) {
      return Scaffold(
        appBar: AppBar(title: Text('Chỉnh sửa hồ sơ', style: AppTheme.heading1)),
        body: const Center(child: CircularProgressIndicator()),
      );
    }
    
    final customer = state.data;

    return Scaffold(
      appBar: AppBar(
        title: Text('Chỉnh sửa hồ sơ', style: AppTheme.heading1),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(AppTheme.pagePadding),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              AppCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Thông tin hệ thống', style: AppTheme.label),
                    const SizedBox(height: 8),
                    Text('Cập nhật thông tin của bạn bên dưới.', style: AppTheme.body),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              
              AppTextField(
                label: 'Họ và tên',
                controller: _fullNameController,
              ),
              const SizedBox(height: 16),
              
              AppTextField(
                label: 'Số điện thoại',
                controller: _phoneController,
                keyboardType: TextInputType.phone,
                readOnly: true,
                obscureText: _obscurePhone,
                suffixIcon: IconButton(
                  icon: Icon(
                    _obscurePhone ? Icons.visibility_off : Icons.visibility,
                    color: AppTheme.subtle,
                  ),
                  onPressed: () {
                    setState(() {
                      _obscurePhone = !_obscurePhone;
                    });
                  },
                ),
              ),
              const SizedBox(height: 16),
              
              AppTextField(
                label: 'Địa chỉ',
                controller: _addressController,
              ),
              const SizedBox(height: 32),
              
              AppFilledButton(
                label: 'Lưu thay đổi',
                onPressed: _saveChanges,
                isLoading: _isLoading,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
