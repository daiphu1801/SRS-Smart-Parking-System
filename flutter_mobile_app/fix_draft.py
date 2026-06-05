import sys, re

file_path = 'lib/features/customer_packages/views/screens/draft_list_screen.dart'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('    final l10n = AppLocalizations.of(context)!;\n    final currencyFormatter', '    final currencyFormatter')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done fixing draft_list_screen warnings')

