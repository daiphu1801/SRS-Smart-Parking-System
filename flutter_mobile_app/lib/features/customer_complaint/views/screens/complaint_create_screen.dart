// Đã gỡ import 'dart:io' để fix lỗi Web Build
import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:http/http.dart' as http;
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:smart_parking_mobile/core/utils/local_storage.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_complaint/viewmodels/complaint_viewmodel.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart' hide LocalStorage;

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
  XFile? _selectedImage;
  Uint8List? _imageBytes;
  final ImagePicker _picker = ImagePicker();

  @override
  void dispose() {
    _titleController.dispose();
    _descController.dispose();
    super.dispose();
  }

  Future<void> _pickImage() async {
    try {
      final pickedFile = await _picker.pickImage(source: ImageSource.gallery, imageQuality: 70);
      if (pickedFile != null) {
        final bytes = await pickedFile.readAsBytes();
        setState(() {
          _selectedImage = pickedFile;
          _imageBytes = bytes;
        });
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Lỗi khi chọn ảnh: $e'), backgroundColor: Colors.red),
      );
    }
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSubmitting = true);

    final l10n = AppLocalizations.of(context)!;
    final vm = context.read<ComplaintViewModel>();
    
    String? uploadedImageUrl;

    try {
      if (_selectedImage != null) {
        final fileName = 'complaint_${DateTime.now().millisecondsSinceEpoch}.jpg';
        final bytes = await _selectedImage!.readAsBytes();
        
        final supabaseUrl = dotenv.env['SUPABASE_URL'];
        final jwt = await LocalStorage.instance.getToken();
        
        final uri = Uri.parse('$supabaseUrl/storage/v1/object/complaint-images/$fileName');
        final req = http.Request('POST', uri);
        req.headers['Authorization'] = 'Bearer $jwt';
        req.headers['Content-Type'] = 'image/jpeg'; // or 'image/png' depending on the file
        req.bodyBytes = bytes;
        
        final response = await req.send();
        if (response.statusCode >= 400) {
          final errStr = await response.stream.bytesToString();
          throw Exception('Upload failed (${response.statusCode}): $errStr');
        }
            
        // Get the full public URL and pass it to the backend
        uploadedImageUrl = Supabase.instance.client.storage
            .from('complaint-images')
            .getPublicUrl(fileName);
      }
      
      final success = await vm.createComplaint(
        title: _titleController.text.trim(),
        description: _descController.text.trim(),
        imageUrl: uploadedImageUrl,
      );

      if (!mounted) return;
      setState(() => _isSubmitting = false);

      if (success) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(l10n.complaintSubmitSuccess),
            backgroundColor: Colors.green,
          ),
        );
        context.pop();
      } else {
        final errorMsg = vm.lastError ?? l10n.complaintSubmitError;
        vm.clearError();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(errorMsg),
            backgroundColor: Colors.red,
          ),
        );
      }
    } catch (e) {
      setState(() => _isSubmitting = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Lỗi tải lên: $e'), backgroundColor: Colors.red),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.submitNewComplaint),
      ),
      body: Form(
        key: _formKey,
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(AppTheme.pagePadding),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                l10n.complaintsListenMessage,
                style: AppTheme.body.copyWith(color: AppTheme.subtle),
              ),
              const SizedBox(height: 24),
              
              AppTextField(
                label: l10n.complaintTitle,
                placeholder: l10n.complaintTitlePlaceholder,
                controller: _titleController,
                validator: (val) {
                  if (val == null || val.trim().isEmpty) {
                    return l10n.pleaseEnterTitle;
                  }
                  return null;
                },
              ),

              const SizedBox(height: 20),

              AppTextField(
                label: l10n.complaintContent,
                placeholder: l10n.complaintContentPlaceholder,
                controller: _descController,
                maxLines: 5,
                validator: (val) {
                  if (val == null || val.trim().isEmpty) {
                    return l10n.pleaseEnterContent;
                  }
                  return null;
                },
              ),

              const SizedBox(height: 24),

              Text(l10n.attachedImages, style: AppTheme.heading3.copyWith(fontSize: 16)),
              const SizedBox(height: 12),
              InkWell(
                onTap: _pickImage,
                borderRadius: BorderRadius.circular(AppTheme.radiusCard),
                child: Container(
                  height: 160,
                  decoration: BoxDecoration(
                    color: AppTheme.surface,
                    borderRadius: BorderRadius.circular(AppTheme.radiusCard),
                    border: Border.all(color: AppTheme.border, style: BorderStyle.solid),
                    image: _imageBytes != null 
                        ? DecorationImage(
                            image: MemoryImage(_imageBytes!), 
                            fit: BoxFit.cover
                          )
                        : null,
                  ),
                  child: _selectedImage == null 
                      ? Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.add_a_photo_outlined, size: 32, color: AppTheme.subtle),
                            const SizedBox(height: 8),
                            Text(l10n.tapToUploadImage,
                                style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
                          ],
                        )
                      : Align(
                          alignment: Alignment.topRight,
                          child: IconButton(
                            icon: const Icon(Icons.close, color: Colors.white),
                            onPressed: () => setState(() {
                              _selectedImage = null;
                              _imageBytes = null;
                            }),
                            style: IconButton.styleFrom(backgroundColor: Colors.black54),
                          ),
                        ),
                ),
              ),

              const SizedBox(height: 32),

              AppFilledButton(
                label: l10n.submitComplaint,
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
