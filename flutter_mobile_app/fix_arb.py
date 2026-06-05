import sys, json

vi_path = 'lib/core/l10n/app_vi.arb'
en_path = 'lib/core/l10n/app_en.arb'

def update_arb(filepath, updates):
    with open(filepath, 'r', encoding='utf-8-sig') as f:
        data = json.load(f)
    
    for k, v in updates.items():
        data[k] = v
        
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

vi_updates = {
  'manageGroupMembers': 'Quản lý thành viên trong nhóm',
  'manageGroupMembersSubtitle': 'Thêm, xóa thành viên thuộc nhóm',
  'memberList': 'Danh sách thành viên',
  'noMembers': 'Chưa có thành viên nào',
  'addMemberPrompt': 'Nhấn Thêm mới để thêm thành viên vào nhóm.',
  'addMember': 'Thêm thành viên',
  'addedNewMember': 'Đã thêm thành viên mới',
  'removeMemberConfirm': 'Xóa thành viên {name} khỏi nhóm?',
  '@removeMemberConfirm': { 'placeholders': { 'name': { 'type': 'String' } } },
  'removedMember': 'Đã xóa thành công',
  'deleteFailed': 'Lỗi khi xóa: {error}',
  '@deleteFailed': { 'placeholders': { 'error': { 'type': 'String' } } },
  'totalMembers': 'Tổng số thành viên',
  'memberCount': '{count} thành viên',
  '@memberCount': { 'placeholders': { 'count': { 'type': 'int' } } },
  'updateMemberSuccess': 'Cập nhật danh sách thành viên thành công!',
  'manageMembers': 'Quản lý thành viên',
  'addToCartFailed': 'Không thể thêm vào giỏ hàng. Vui lòng thử lại!',
  'clearCart': 'Xóa giỏ hàng',
  'clearCartConfirm': 'Bạn có chắc chắn muốn xóa các xe đã chọn khỏi giỏ hàng?',
  'cart': 'Giỏ hàng',
  'cartEmpty': 'Giỏ hàng trống',
  'cartEmptySubtitle': 'Hiện tại chưa có phương tiện nào trong giỏ hàng.',
  'viewCart': 'Xem giỏ hàng',
  'confirm': 'Xác nhận',
  'delete': 'Xóa'
}

en_updates = {
  'manageGroupMembers': 'Manage group members',
  'manageGroupMembersSubtitle': 'Add or remove members from the group',
  'memberList': 'Member list',
  'noMembers': 'No members yet',
  'addMemberPrompt': 'Tap Add new to add members to the group.',
  'addMember': 'Add member',
  'addedNewMember': 'Added new member',
  'removeMemberConfirm': 'Remove member {name} from the group?',
  '@removeMemberConfirm': { 'placeholders': { 'name': { 'type': 'String' } } },
  'removedMember': 'Deleted successfully',
  'deleteFailed': 'Failed to delete: {error}',
  '@deleteFailed': { 'placeholders': { 'error': { 'type': 'String' } } },
  'totalMembers': 'Total members',
  'memberCount': '{count} members',
  '@memberCount': { 'placeholders': { 'count': { 'type': 'int' } } },
  'updateMemberSuccess': 'Member list updated successfully!',
  'manageMembers': 'Manage members',
  'addToCartFailed': 'Failed to add to cart. Please try again!',
  'clearCart': 'Clear cart',
  'clearCartConfirm': 'Are you sure you want to remove selected vehicles from the cart?',
  'cart': 'Cart',
  'cartEmpty': 'Cart is empty',
  'cartEmptySubtitle': 'There are currently no vehicles in the cart.',
  'viewCart': 'View cart',
  'confirm': 'Confirm',
  'delete': 'Delete'
}

update_arb(vi_path, vi_updates)
update_arb(en_path, en_updates)
print('Done fixing ARB files')

