package com.smartparking.identity.service.admin;

import com.smartparking.identity.entity.Role;
import com.smartparking.identity.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final RoleRepository roleRepo;

    public List<Role> getAllRoles(String search) {
        return roleRepo.findAll(); // Add search logic later
    }
}
