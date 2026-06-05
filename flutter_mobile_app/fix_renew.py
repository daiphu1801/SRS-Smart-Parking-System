import sys, re

file_path = 'lib/features/customer_packages/views/screens/renew_package_screen.dart'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add l10n to _submitRenewal
if 'final l10n = AppLocalizations.of(context)!;' not in content.split('void _submitRenewal(BookingDetail oldDetail, double totalPrice) async {')[1].split('}')[0]:
    content = content.replace('void _submitRenewal(BookingDetail oldDetail, double totalPrice) async {', 'void _submitRenewal(BookingDetail oldDetail, double totalPrice) async {\n    final l10n = AppLocalizations.of(context)!;')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done renew_package_screen')

