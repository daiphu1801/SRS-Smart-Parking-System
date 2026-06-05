import sys

file_path = 'lib/features/customer_account/views/screens/group_detail_screen.dart'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(""import 'package:smart_parking_mobile/core/utils/view_state.dart';"", ""import 'package:smart_parking_mobile/core/utils/view_state.dart';\nimport 'package:smart_parking_mobile/core/l10n/app_localizations.dart';"")

content = content.replace(""Widget build(BuildContext context) {"", ""Widget build(BuildContext context) {\n    final l10n = AppLocalizations.of(context)!;"")

content = content.replace(""'Tổng số thành viên'"", ""l10n.totalMembers"")
content = content.replace(""'\ thành viên'"", ""l10n.memberCount(group.customers.length)"")
content = content.replace(""'Danh sách thành viên'"", ""l10n.memberList"")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Replaced group_detail')
