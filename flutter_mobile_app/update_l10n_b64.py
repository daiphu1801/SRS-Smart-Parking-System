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

# Base class additions
base_append = '''
  String get manageGroupMembers;
  String get manageGroupMembersSubtitle;
  String get memberList;
  String get noMembers;
  String get addMemberPrompt;
  String get addMember;
  String get addedNewMember;
  String removeMemberConfirm(String name);
  String get removedMember;
  String deleteFailed(String error);
  String get totalMembers;
  String memberCount(int count);
  String get updateMemberSuccess;
  String get manageMembers;
  String get addToCartFailed;
  String get clearCart;
  String get clearCartConfirm;
  String get cart;
  String get cartEmpty;
  String get cartEmptySubtitle;
  String get viewCart;
'''
append_to_file('lib/core/l10n/app_localizations.dart', base64.b64encode(base_append.encode('utf-8')).decode('utf-8'))

# VI additions
vi_append = '''
  @override
  String get manageGroupMembers => 'Quản lý thành viên trong nhóm';
  @override
  String get manageGroupMembersSubtitle => 'Thêm, xóa thành viên thuộc nhóm';
  @override
  String get memberList => 'Danh sách thành viên';
  @override
  String get noMembers => 'Chưa có thành viên nào';
  @override
  String get addMemberPrompt => 'Nhấn Thêm mới để thêm thành viên vào nhóm.';
  @override
  String get addMember => 'Thêm thành viên';
  @override
  String get addedNewMember => 'Đã thêm thành viên mới';
  @override
  String removeMemberConfirm(String name) => 'Xóa thành viên \ khỏi nhóm?';
  @override
  String get removedMember => 'Đã xóa thành công';
  @override
  String deleteFailed(String error) => 'Lỗi khi xóa: \';
  @override
  String get totalMembers => 'Tổng số thành viên';
  @override
  String memberCount(int count) => '\ thành viên';
  @override
  String get updateMemberSuccess => 'Cập nhật danh sách thành viên thành công!';
  @override
  String get manageMembers => 'Quản lý thành viên';
  @override
  String get addToCartFailed => 'Không thể thêm vào giỏ hàng. Vui lòng thử lại!';
  @override
  String get clearCart => 'Xóa giỏ hàng';
  @override
  String get clearCartConfirm => 'Bạn có chắc chắn muốn xóa các xe đã chọn khỏi giỏ hàng?';
  @override
  String get cart => 'Giỏ hàng';
  @override
  String get cartEmpty => 'Giỏ hàng trống';
  @override
  String get cartEmptySubtitle => 'Hiện tại chưa có phương tiện nào trong giỏ hàng.';
  @override
  String get viewCart => 'Xem giỏ hàng';
'''
append_to_file('lib/core/l10n/app_localizations_vi.dart', base64.b64encode(vi_append.encode('utf-8')).decode('utf-8'))

# EN additions
en_append = '''
  @override
  String get manageGroupMembers => 'Manage group members';
  @override
  String get manageGroupMembersSubtitle => 'Add or remove members from the group';
  @override
  String get memberList => 'Member list';
  @override
  String get noMembers => 'No members yet';
  @override
  String get addMemberPrompt => 'Tap Add new to add members to the group.';
  @override
  String get addMember => 'Add member';
  @override
  String get addedNewMember => 'Added new member';
  @override
  String removeMemberConfirm(String name) => 'Remove member \ from the group?';
  @override
  String get removedMember => 'Deleted successfully';
  @override
  String deleteFailed(String error) => 'Failed to delete: \';
  @override
  String get totalMembers => 'Total members';
  @override
  String memberCount(int count) => '\ members';
  @override
  String get updateMemberSuccess => 'Member list updated successfully!';
  @override
  String get manageMembers => 'Manage members';
  @override
  String get addToCartFailed => 'Failed to add to cart. Please try again!';
  @override
  String get clearCart => 'Clear cart';
  @override
  String get clearCartConfirm => 'Are you sure you want to remove selected vehicles from the cart?';
  @override
  String get cart => 'Cart';
  @override
  String get cartEmpty => 'Cart is empty';
  @override
  String get cartEmptySubtitle => 'There are currently no vehicles in the cart.';
  @override
  String get viewCart => 'View cart';
'''
append_to_file('lib/core/l10n/app_localizations_en.dart', base64.b64encode(en_append.encode('utf-8')).decode('utf-8'))

print('Done updating Dart localization files')

