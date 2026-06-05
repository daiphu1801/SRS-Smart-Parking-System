import sys, re

file_path = 'lib/features/customer_packages/views/screens/draft_list_screen.dart'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add l10n to _deleteSelected
if 'final l10n = AppLocalizations.of(context)!;' not in content.split('Future<void> _deleteSelected() async {')[1].split('}')[0]:
    content = content.replace('Future<void> _deleteSelected() async {', 'Future<void> _deleteSelected() async {\n    final l10n = AppLocalizations.of(context)!;')

# Fix const AppEmptyState
content = re.sub(r'return const AppEmptyState\(\s*icon: Icons\.directions_car_outlined,\s*title: \'Giỏ hàng trống\',', 
                 'return AppEmptyState(\\n                icon: Icons.directions_car_outlined,\\n                title: l10n.cartEmpty,', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done draft_list_screen')

