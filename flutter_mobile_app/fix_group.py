import sys, re

file_path = 'lib/features/customer_account/views/screens/group_edit_screen.dart'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace Quản lý nhóm (Qun lA nhA3m) with l10n.manageMembers
content = re.sub(r'title: Text\([^,]+nhA3m[^,]+,\s*style:\s*AppTheme\.heading1\)', 'title: Text(l10n.manageMembers, style: AppTheme.heading1)', content)

# Replace Quản lý thành viên (Qun lA thAnh viAn) with l10n.manageMembers
content = re.sub(r'title: Text\([^,]+viAn[^,]+,\s*style:\s*AppTheme\.heading1\)', 'title: Text(l10n.manageMembers, style: AppTheme.heading1)', content)

# Bring back final l10n = AppLocalizations.of(context)!; if I removed it
if 'final l10n = AppLocalizations.of(context)!;' not in content:
    content = content.replace('Widget build(BuildContext context) {\n    final state', 'Widget build(BuildContext context) {\n    final l10n = AppLocalizations.of(context)!;\n    final state')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done fixing group_edit_screen')

