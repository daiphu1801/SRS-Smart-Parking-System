package com.smartparking.identity.service.admin;

import com.smartparking.identity.dto.request.GroupsCustomerCreateRequest;
import com.smartparking.identity.dto.response.CustomerResponse;
import com.smartparking.identity.dto.response.GroupsCustomerResponse;
import com.smartparking.identity.entity.Account;
import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.GroupsCustomer;
import com.smartparking.identity.entity.GroupsProfile;
import com.smartparking.identity.repository.AccountRepository;
import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.identity.repository.GroupsCustomersRepository;
import com.smartparking.identity.repository.GroupsProfileRepository;
import com.smartparking.identity.service.AuthService;
import com.smartparking.identity.specification.CustomerSpecs;
import com.smartparking.identity.specification.GroupsCustomerSpecs;
import com.smartparking.shared.dto.PageResponse;
import com.smartparking.shared.exception.BusinessException;
import com.smartparking.shared.integration.SupabaseAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCustomerGroupService {

    private final GroupsCustomersRepository customerGroupRepo;
    private final GroupsProfileRepository groupProfileRepo;
    private final AccountRepository accountRepository;
    private final AuthService authService;

    public PageResponse<GroupsCustomerResponse> getCustomerGroups(Pageable pageable,  GroupsCustomer filter,String masterAccountName,String masterAccountphone) {
        Specification<GroupsCustomer> spec = Specification
                .where(GroupsCustomerSpecs.fetchRelations())
                .and(GroupsCustomerSpecs.hasId(filter.getId()))
                .and(GroupsCustomerSpecs.isSynchronize(filter.getIsSynchronize()))
                .and(GroupsCustomerSpecs.hasMasterPhone(masterAccountphone))
                .and(GroupsCustomerSpecs.hasMasterAccountId(filter.getMasterAccountId()))
                .and(GroupsCustomerSpecs.hasProfileId(filter.getProfileId()));


        Page<GroupsCustomer> page = customerGroupRepo.findAll(spec,pageable);

        List<GroupsCustomerResponse> content = page.getContent().stream().map(groupCustomer -> GroupsCustomerResponse.builder()
                .id(groupCustomer.getId())
                .groupName(groupCustomer.getGroupName())
                .groupCode(groupCustomer.getGroupCode())
                .profileId(groupCustomer.getProfileId())
                .isSynchronize(groupCustomer.getIsSynchronize())
                .profileCode(groupCustomer.getGroupsProfile().getProfileCode())
                .profileName(groupCustomer.getGroupsProfile().getProfileName())
                .masterAccountId(groupCustomer.getMasterAccountId())
                .createdDate(groupCustomer.getCreatedAt())

                .masterPhone(groupCustomer.getMasterAccount() != null ? groupCustomer.getMasterAccount().getUsername() : null)
                .build()).collect(Collectors.toList());
        return new PageResponse<>(content, page.getTotalElements(), page.getTotalPages());

    }

    public GroupsCustomer createCustomerGroup(GroupsCustomerCreateRequest request) {
        GroupsCustomer groupsCustomer = new GroupsCustomer();
        groupsCustomer.setGroupName(request.getGroupName());
        groupsCustomer.setGroupCode(request.getGroupCode());
        groupsCustomer.setCreatedAt(request.getCreatedAt());
        groupsCustomer.setCreatedBy(request.getCreatedBy());
        groupsCustomer.setIsSynchronize(request.getIsSynchronize());

        GroupsProfile profile = groupProfileRepo.findById(request.getProfileId())
                .orElseThrow(() -> new BusinessException("Lỗi: Loại hình Profile này không tồn tại!"));
        groupsCustomer.setGroupsProfile(profile);

        if (request.getMasterAccountId() != null) {
            Account account = accountRepository.findById(request.getMasterAccountId())
                    .orElseThrow(() -> new RuntimeException("Lỗi: Tài khoản chủ hộ này không tồn tại!"));
            groupsCustomer.setMasterAccount(account);
        }

        return customerGroupRepo.save(groupsCustomer);
    }

    @Transactional
    public GroupsCustomerResponse updateCustomerGroup(Integer id, GroupsCustomer request) { // Khuyên thật: Đoạn này nên dùng DTO thay vì Entity
        GroupsCustomer group = customerGroupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer group not found"));

        Account oldMaster = group.getMasterAccount();
        Integer oldMasterId = (oldMaster != null) ? oldMaster.getId() : null;

        Integer newMasterId = request.getMasterAccountId();

        group.setGroupName(request.getGroupName());
        group.setGroupCode(request.getGroupCode());
        group.setIsSynchronize(request.getIsSynchronize());

        boolean isMasterChanged = false;
        Account newMaster = null;

        if (newMasterId != null && !newMasterId.equals(oldMasterId)) {
            newMaster = accountRepository.findById(newMasterId)
                    .orElseThrow(() -> new RuntimeException("Chủ hộ mới không tồn tại"));
            group.setMasterAccount(newMaster);
            isMasterChanged = true;
        } else if (newMasterId == null && oldMasterId != null) {
            group.setMasterAccount(null);
            isMasterChanged = true;
        }


        GroupsCustomer savedGroup = customerGroupRepo.save(group);

        GroupsCustomerResponse dto = GroupsCustomerResponse.builder()
                .id(savedGroup.getId())
                .groupName(savedGroup.getGroupName())
                .groupCode(savedGroup.getGroupCode())
                .masterAccountId(savedGroup.getMasterAccountId())
                .profileName(savedGroup.getGroupsProfile() != null ? savedGroup.getGroupsProfile().getProfileName() : null)
                .build();

        if (isMasterChanged) {
            if (oldMaster != null && oldMaster.getSupabaseId() != null) {
                authService.syncMetadataToSupabase(oldMaster);
            }

            if (newMaster != null && newMaster.getSupabaseId() != null) {
                authService.syncMetadataToSupabase(newMaster);
            }
        }


        return dto;
    }

    public void deleteCustomerGroup(Integer id) {
        customerGroupRepo.deleteById(id);
    }
}
