package com.smartparking.identity.repository;

import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.GroupsProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupsProfileRepository extends JpaRepository<GroupsProfile, Integer>, JpaSpecificationExecutor<GroupsProfile> {
}
