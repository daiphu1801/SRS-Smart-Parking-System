package com.smartparking.identity.service.admin;

import com.smartparking.identity.entity.GroupsCustomer;
import com.smartparking.identity.repository.GroupsCustomersRepository;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCustomerGroupService {

    private final GroupsCustomersRepository customerGroupRepo;

    public PageResponse<GroupsCustomer> getCustomerGroups(Pageable pageable, String search, Integer profileId) {
        Page<GroupsCustomer> page = customerGroupRepo.findAll(pageable); // Add filtering logic later
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages());
    }

    public GroupsCustomer createCustomerGroup(GroupsCustomer group) {
        return customerGroupRepo.save(group);
    }

    public GroupsCustomer updateCustomerGroup(Integer id, GroupsCustomer groupUpdates) {
        GroupsCustomer group = customerGroupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer group not found"));
        group.setGroupName(groupUpdates.getGroupName());
        return customerGroupRepo.save(group);
    }

    public void deleteCustomerGroup(Integer id) {
        customerGroupRepo.deleteById(id);
    }
}
