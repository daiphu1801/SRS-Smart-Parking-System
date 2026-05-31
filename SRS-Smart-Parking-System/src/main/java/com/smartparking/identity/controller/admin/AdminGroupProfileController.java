package com.smartparking.identity.controller.admin;

import com.smartparking.identity.dto.request.GroupsProfileCreateRequest;
import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.GroupsProfile;
import com.smartparking.identity.service.admin.AdminGroupProfileService;
import com.smartparking.shared.dto.ApiResponse;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/group-profiles")
@RequiredArgsConstructor
public class AdminGroupProfileController {

    private final AdminGroupProfileService groupProfileService;
    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GroupsProfile>>> listGroupProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute GroupsProfile filter) {
        return ResponseEntity.ok(ApiResponse.success(groupProfileService.getGroupProfiles(PageRequest.of(page, size), filter)));
    }

    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<GroupsProfile>> createGroupProfile(@RequestBody GroupsProfileCreateRequest profile) {
        return ResponseEntity.status(201).body(ApiResponse.success("Tạo Group Profile thành công", groupProfileService.createGroupProfile(profile)));
    }

    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupsProfile>> updateGroupProfile(@PathVariable Integer id, @RequestBody GroupsProfile profile) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật Group Profile thành công", groupProfileService.updateGroupProfile(id, profile)));
    }

    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroupProfile(@PathVariable Integer id) {
        groupProfileService.deleteGroupProfile(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa Group Profile thành công"));
    }
}
