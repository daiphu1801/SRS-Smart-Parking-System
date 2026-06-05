import sys

properties = [
    ('String get manageGroupMembers;', ""String get manageGroupMembers => 'Quản lý thành viên trong nhóm';"", ""String get manageGroupMembers => 'Manage group members';""),
    ('String get manageGroupMembersSubtitle;', ""String get manageGroupMembersSubtitle => 'Thêm, xóa thành viên thuộc nhóm';"", ""String get manageGroupMembersSubtitle => 'Add or remove members from the group';""),
    ('String get memberList;', ""String get memberList => 'Danh sách thành viên';"", ""String get memberList => 'Member list';""),
    ('String get noMembers;', ""String get noMembers => 'Chưa có thành viên nào';"", ""String get noMembers => 'No members yet';""),
    ('String get addMemberPrompt;', ""String get addMemberPrompt => 'Nhấn Thêm mới để thêm thành viên vào nhóm.';"", ""String get addMemberPrompt => 'Tap Add new to add members to the group.';""),
    ('String get addMember;', ""String get addMember => 'Thêm thành viên';"", ""String get addMember => 'Add member';""),
    ('String get addedNewMember;', ""String get addedNewMember => 'Đã thêm thành viên mới';"", ""String get addedNewMember => 'Added new member';""),
    ('String removeMemberConfirm(String name);', ""String removeMemberConfirm(String name) => 'Xóa thành viên \ khỏi nhóm?';"", ""String removeMemberConfirm(String name) => 'Remove member \ from the group?';""),
    ('String get removedMember;', ""String get removedMember => 'Đã xóa thành công';"", ""String get removedMember => 'Deleted successfully';""),
    ('String deleteFailed(String error);', ""String deleteFailed(String error) => 'Lỗi khi xóa: \';"", ""String deleteFailed(String error) => 'Failed to delete: \';""),
    ('String get totalMembers;', ""String get totalMembers => 'Tổng số thành viên';"", ""String get totalMembers => 'Total members';""),
    ('String memberCount(int count);', ""String memberCount(int count) => '\ thành viên';"", ""String memberCount(int count) => '\ members';""),
    ('String get updateMemberSuccess;', ""String get updateMemberSuccess => 'Cập nhật danh sách thành viên thành công!';"", ""String get updateMemberSuccess => 'Member list updated successfully!';""),
    ('String get manageMembers;', ""String get manageMembers => 'Quản lý thành viên';"", ""String get manageMembers => 'Manage members';""),
    ('String get addToCartFailed;', ""String get addToCartFailed => 'Không thể thêm vào giỏ hàng. Vui lòng thử lại!';"", ""String get addToCartFailed => 'Failed to add to cart. Please try again!';""),
    ('String get clearCart;', ""String get clearCart => 'Xóa giỏ hàng';"", ""String get clearCart => 'Clear cart';""),
    ('String get clearCartConfirm;', ""String get clearCartConfirm => 'Bạn có chắc chắn muốn xóa các xe đã chọn khỏi giỏ hàng?';"", ""String get clearCartConfirm => 'Are you sure you want to remove selected vehicles from the cart?';""),
    ('String get cart;', ""String get cart => 'Giỏ hàng';"", ""String get cart => 'Cart';""),
    ('String get cartEmpty;', ""String get cartEmpty => 'Giỏ hàng trống';"", ""String get cartEmpty => 'Cart is empty';""),
    ('String get cartEmptySubtitle;', ""String get cartEmptySubtitle => 'Hiện tại chưa có phương tiện nào trong giỏ hàng.';"", ""String get cartEmptySubtitle => 'There are currently no vehicles in the cart.';""),
    ('String get viewCart;', ""String get viewCart => 'Xem giỏ hàng';"", ""String get viewCart => 'View cart';"")
]

def append_to_file(filepath, content_to_append):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Insert right before the last '}'
    last_brace_idx = content.rfind('}')
    if last_brace_idx != -1:
        new_content = content[:last_brace_idx] + content_to_append + '\n}\n'
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)

base_append = '\n' + '\n'.join('  ' + p[0] for p in properties)
append_to_file('lib/core/l10n/app_localizations.dart', base_append)

vi_append = '\n  @override\n' + '\n  @override\n'.join('  ' + p[1] for p in properties)
append_to_file('lib/core/l10n/app_localizations_vi.dart', vi_append)

en_append = '\n  @override\n' + '\n  @override\n'.join('  ' + p[2] for p in properties)
append_to_file('lib/core/l10n/app_localizations_en.dart', en_append)

print('Done updating Dart localization files')
