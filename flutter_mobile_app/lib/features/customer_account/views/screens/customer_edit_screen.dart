import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_account/viewmodels/customer_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_account/models/customer_models.dart';

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
  late TextEditingController _emailController;
  late TextEditingController _addressController;
  
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _fullNameController = TextEditingController();
    _phoneController = TextEditingController();
    _emailController = TextEditingController();
    _addressController = TextEditingController();

    // Populate data from ViewModel
    final state = context.read<CustomerViewModel>().customerState;
    if (state is Success<Customer>) {
      final customer = state.data;
      _fullNameController.text = customer.fullName;
      _phoneController.text = customer.phone;
      _emailController.text = customer.email;
      _addressController.text = customer.address;
    }
  }

  @override
  void dispose() {
    _fullNameController.dispose();
    _phoneController.dispose();
    _emailController.dispose();
    _addressController.dispose();
    super.dispose();
  }

  Future<void> _saveChanges() async {
    if (_fullNameController.text.trim().isEmpty || 
        _phoneController.text.trim().isEmpty || 
        _emailController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng điền đầy đủ các trường bắt buộc')),
      );
      return;
    }
    
    setState(() => _isLoading = true);
    
    final viewModel = context.read<CustomerViewModel>();
    final currentState = viewModel.customerState;
    if (currentState is Success<Customer>) {
      final updatedCustomer = currentState.data.copyWith(
        fullName: _fullNameController.text.trim(),
        phone: _phoneController.text.trim(),
        email: _emailController.text.trim(),
        address: _addressController.text.trim(),
      );
      
      final success = await viewModel.updateCustomer(widget.customerId, updatedCustomer);
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
    final state = context.watch<CustomerViewModel>().customerState;
    
    // Ensure we have data
    if (state is! Success<Customer>) {
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
              // Username and Identity Number are usually non-editable
              AppCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Thông tin hệ thống', style: AppTheme.label),
                    const SizedBox(height: 8),
                    Text('Tên đăng nhập: ${customer.username}', style: AppTheme.body),
                    const SizedBox(height: 4),
                    Text('Số CMND/CCCD: ${customer.identityNumber}', style: AppTheme.body),
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
              ),
              const SizedBox(height: 16),
              
              AppTextField(
                label: 'Email',
                controller: _emailController,
                keyboardType: TextInputType.emailAddress,
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
