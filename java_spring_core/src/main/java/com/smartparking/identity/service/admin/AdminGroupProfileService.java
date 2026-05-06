package com.smartparking.identity.service.admin;

import com.smartparking.identity.entity.GroupsProfile;
import com.smartparking.identity.repository.GroupsProfileRepository;
import com.smartparking.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminGroupProfileService {

    private final GroupsProfileRepository groupProfileRepo;

    public PageResponse<GroupsProfile> getGroupProfiles(Pageable pageable, String search) {
        Page<GroupsProfile> page = groupProfileRepo.findAll(pageable); // Add search logic later when JPA spec is ready
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages());
    }

    public GroupsProfile createGroupProfile(GroupsProfile profile) {
        return groupProfileRepo.save(profile);
    }

    public GroupsProfile updateGroupProfile(Integer id, GroupsProfile profileUpdates) {
        GroupsProfile profile = groupProfileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group profile not found"));
        // Handle updates here
        profile.setProfileName(profileUpdates.getProfileName());
        profile.setProfileCode(profileUpdates.getProfileCode());
        return groupProfileRepo.save(profile);
    }

    public void deleteGroupProfile(Integer id) {
        groupProfileRepo.deleteById(id);
    }
}
