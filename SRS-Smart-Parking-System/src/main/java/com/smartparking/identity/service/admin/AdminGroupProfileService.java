package com.smartparking.identity.service.admin;

import com.smartparking.identity.dto.request.GroupsProfileCreateRequest;
import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.GroupsProfile;
import com.smartparking.identity.repository.GroupsProfileRepository;
import com.smartparking.identity.specification.CustomerSpecs;
import com.smartparking.identity.specification.GroupsProfileSpecs;
import com.smartparking.shared.dto.PageResponse;
import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminGroupProfileService {

    private final GroupsProfileRepository groupProfileRepo;

    public PageResponse<GroupsProfile> getGroupProfiles(Pageable pageable, GroupsProfile filter) {
        Specification<GroupsProfile> spec = Specification
                .where(GroupsProfileSpecs.hasId(filter.getId()))
                .and(GroupsProfileSpecs.hasProfileCode(filter.getProfileCode()))
                .and(GroupsProfileSpecs.hasProfileName(filter.getProfileName()));


        Page<GroupsProfile> page = groupProfileRepo.findAll(spec,pageable);
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages());
    }

    public GroupsProfile createGroupProfile(GroupsProfileCreateRequest profile) {
        GroupsProfile groupProfile = new GroupsProfile();
        groupProfile.setProfileCode(profile.getProfileCode());
        groupProfile.setProfileName(profile.getProfileName());
        return groupProfileRepo.save(groupProfile);
    }

    public GroupsProfile updateGroupProfile(Integer id, GroupsProfile profileUpdates) {
        GroupsProfile profile = groupProfileRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Group profile not found"));
        // Handle updates here
        profile.setProfileName(profileUpdates.getProfileName());
        profile.setProfileCode(profileUpdates.getProfileCode());
        return groupProfileRepo.save(profile);
    }

    public void deleteGroupProfile(Integer id) {
        groupProfileRepo.deleteById(id);
    }
}
