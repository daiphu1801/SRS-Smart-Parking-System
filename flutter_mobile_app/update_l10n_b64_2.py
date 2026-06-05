import sys, base64

def append_to_file(filepath, content_to_append_b64):
    content_to_append = base64.b64decode(content_to_append_b64).decode('utf-8')
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    last_brace_idx = content.rfind('}')
    if last_brace_idx != -1:
        new_content = content[:last_brace_idx] + content_to_append + '\n}\n'
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)

base_append = '''
  String get confirm;
  String get delete;
'''
append_to_file('lib/core/l10n/app_localizations.dart', base64.b64encode(base_append.encode('utf-8')).decode('utf-8'))

vi_append = '''
  @override
  String get confirm => 'Xác nhận';
  @override
  String get delete => 'Xóa';
'''
append_to_file('lib/core/l10n/app_localizations_vi.dart', base64.b64encode(vi_append.encode('utf-8')).decode('utf-8'))

en_append = '''
  @override
  String get confirm => 'Confirm';
  @override
  String get delete => 'Delete';
'''
append_to_file('lib/core/l10n/app_localizations_en.dart', base64.b64encode(en_append.encode('utf-8')).decode('utf-8'))

print('Done updating Dart localization files for confirm and delete')

