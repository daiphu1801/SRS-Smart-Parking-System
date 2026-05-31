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
import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new BusinessException("Không tìm thấy Role với ID: " + roleId));

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

//        saveRolePermissions(savedRole, request.getPermissions());

        return savedRole;
    }

    @Transactional
    public Role updateRole(Integer roleId, RoleUpsertRequest request) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy Role với ID: " + roleId));

        existingRole.setRoleName(request.getRoleName());
         existingRole.setDescription(request.getDescription());
        Role savedRole = roleRepository.save(existingRole);

        List<RoleFunctionAction> oldPermissions = roleFunctionActionRepo.findByRoleId(roleId);

        Set<String> newSignatures = request.getPermissions().stream()
                .map(p -> p.getFuncId() + "_" + p.getActionId())
                .collect(Collectors.toSet());

        Set<String> oldSignatures = oldPermissions.stream()
                .map(old -> old.getFuncId() + "_" + old.getActionId())
                .collect(Collectors.toSet());

        // 4. TÌM TẬP CẦN XÓA (Có trong Cũ nhưng không có trong Mới)
        List<RoleFunctionAction> toDelete = oldPermissions.stream()
                .filter(old -> !newSignatures.contains(old.getFuncId() + "_" + old.getActionId()))
                .collect(Collectors.toList());

        // 5. TÌM TẬP CẦN THÊM MỚI (Có trong Mới nhưng không có trong Cũ)
        List<RoleFunctionAction> toInsert = request.getPermissions().stream()
                .filter(p -> !oldSignatures.contains(p.getFuncId() + "_" + p.getActionId()))
                .map(p -> {
                    RoleFunctionAction newAction = new RoleFunctionAction();
                    
                    // Set Entity Proxies
                    newAction.setRole(existingRole);
                    newAction.setFunction(functionRepository.getReferenceById(p.getFuncId()));
                    newAction.setAction(actionRepository.getReferenceById(p.getActionId()));
                    
                    // Set primitive IDs for Composite Key
                    newAction.setRoleId(roleId);
                    newAction.setFuncId(p.getFuncId());
                    newAction.setActionId(p.getActionId());
                    
                    return newAction;
                })
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            roleFunctionActionRepo.deleteAll(toDelete);
        }

        if (!toInsert.isEmpty()) {
            roleFunctionActionRepo.saveAll(toInsert);
        }

        return savedRole;
    }

    // 6. HÀM XÓA MỀM (SOFT DELETE)
    @Transactional
    public void deleteRole(Integer roleId) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy Role với ID: " + roleId));

         existingRole.setIsActive(false);

        roleRepository.save(existingRole);
    }
    @Transactional
    public void hardDeleteRole(Integer roleId) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy Role với ID: " + roleId));
        try{

        roleRepository.delete(existingRole);
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa cứng role");
        }
    }


}

