import sys, re

file_path = 'lib/features/customer_packages/views/screens/draft_list_screen.dart'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix the build method of _DraftListScreenState
content = content.replace('Widget build(BuildContext context) {\n    final currencyFormatter', 'Widget build(BuildContext context) {\n    final l10n = AppLocalizations.of(context)!;\n    final currencyFormatter')

# Remove l10n from _DraftSelectAllRow
content = content.replace('Widget build(BuildContext context) {\n    final l10n = AppLocalizations.of(context)!;\n    return GestureDetector', 'Widget build(BuildContext context) {\n    return GestureDetector')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done fixing draft_list_screen')

