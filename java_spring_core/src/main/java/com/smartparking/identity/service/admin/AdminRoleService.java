package com.smartparking.identity.service.admin;

import com.smartparking.identity.dto.request.RoleUpsertRequest;
import com.smartparking.identity.dto.response.RoleDetailResponse;
import com.smartparking.identity.dto.response.SystemFunctionActionResponse;
import com.smartparking.identity.entity.Role;
import com.smartparking.identity.entity.RoleFunctionAction;
import com.smartparking.identity.repository.ActionRepository;
import com.smartparking.identity.repository.FunctionRepository;
import com.smartparking.identity.repository.RoleFunctionActionRepository;
import com.smartparking.identity.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final RoleRepository roleRepository;
    private final FunctionRepository functionRepository;
    private final ActionRepository actionRepository;
    private final RoleFunctionActionRepository roleFunctionActionRepo;

    public List<Role> getAllRoles(String search) {
        return roleRepository.findAll(); // Add search logic later

    }

    @Transactional(readOnly = true)
    public SystemFunctionActionResponse getAllFunctionAndAction() {

        // Lấy tất cả và map sang DTO cho nhẹ
        List<SystemFunctionActionResponse.FunctionDto> functions = functionRepository.findAll().stream()
                .map(f -> SystemFunctionActionResponse.FunctionDto.builder()
                        .id(f.getId())
                        .code(f.getFunctionCode()) // Cột trong Entity của ông
                        .name(f.getDescription()) // Cột trong Entity của ông
                        .build())
                .toList();

        List<SystemFunctionActionResponse.ActionDto> actions = actionRepository.findAll().stream()
                .map(a -> SystemFunctionActionResponse.ActionDto.builder()
                        .id(a.getId())
                        .code(a.getActionCode())
                        .name(a.getDescription())
                        .build())
                .toList();

        return SystemFunctionActionResponse.builder()
                .functions(functions)
                .actions(actions)
                .build();
    }

    // -------------------------------------------------------------
    // HÀM MỚI 2: Lấy chi tiết Role và danh sách Quyền đã cấp
    // -------------------------------------------------------------
    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleDetail(Integer roleId) {

        // 1. Lấy thông tin cơ bản của Role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Role với ID: " + roleId));

        // 2. Vào bảng trung gian (RoleFunctionAction) lấy các quyền đã gán
        List<RoleFunctionAction> rolePermissions = roleFunctionActionRepo.findByRoleId(roleId);

        // 3. Map sang DTO
        List<RoleDetailResponse.RolePermissionDto> permissionDtos = rolePermissions.stream()
                .map(rp -> RoleDetailResponse.RolePermissionDto.builder()
                        .funcId(rp.getFuncId())
                        .actionId(rp.getActionId())
                        // Nếu entity của ông đã setup @ManyToOne chuẩn, có thể lôi tên ghép ra luôn:
                        .permissionCode(rp.getFunction().getFunctionCode() + "_" + rp.getAction().getActionCode())
                        .build())
                .toList();

        return RoleDetailResponse.builder()
                .roleId(role.getId())
                .roleName(role.getRoleName())
                // .description(role.getDescription()) // Nếu có
                .permissions(permissionDtos)
                .build();
    }


    @Transactional
    public Role createRole(RoleUpsertRequest request) {
        Role newRole = new Role();
        newRole.setRoleName(request.getRoleName());
        // newRole.setDescription(request.getDescription());
        // newRole.setIsActive(true);

        Role savedRole = roleRepository.save(newRole);

        saveRolePermissions(savedRole, request.getPermissions());

        return savedRole;
    }

    // 5. HÀM SỬA ROLE
    @Transactional
    public Role updateRole(Integer roleId, RoleUpsertRequest request) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Role với ID: " + roleId));

        existingRole.setRoleName(request.getRoleName());
        // existingRole.setDescription(request.getDescription());
        Role savedRole = roleRepository.save(existingRole);

        // Xóa sạch quyền cũ
        roleFunctionActionRepo.deleteByRoleId(roleId);

        // Nạp lại quyền mới
        saveRolePermissions(savedRole, request.getPermissions());

        return savedRole;
    }

    // 6. HÀM XÓA MỀM (SOFT DELETE)
    @Transactional
    public void deleteRole(Integer roleId) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Role với ID: " + roleId));

         existingRole.setIsActive(false);

        roleRepository.save(existingRole);
    }

    private void saveRolePermissions(Role role, List<RoleUpsertRequest.PermissionRequest> permissions) {
        if (permissions == null || permissions.isEmpty()) return;

        List<RoleFunctionAction> rolePermissions = permissions.stream()
                .map(p -> {
                    RoleFunctionAction rfa = new RoleFunctionAction();

                    rfa.setRole(role);

                    // Dùng getReferenceById để tạo Proxy, không tốn query SELECT dưới DB
                    rfa.setFunction(functionRepository.getReferenceById(p.getFuncId()));
                    rfa.setAction(actionRepository.getReferenceById(p.getActionId()));

                    // 2. SET CẢ PRIMITIVE ID CHO ID_CLASS (Vì Composite Key IdClass đôi khi bắt buộc phải có giá trị này)
                    rfa.setRoleId(role.getId());
                    rfa.setFuncId(p.getFuncId());
                    rfa.setActionId(p.getActionId());

                    return rfa;
                })
                .toList();

        roleFunctionActionRepo.saveAll(rolePermissions);
    }
}

