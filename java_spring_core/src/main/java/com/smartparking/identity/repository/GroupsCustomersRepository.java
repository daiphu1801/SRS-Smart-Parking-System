package com.smartparking.identity.repository;

import com.smartparking.identity.entity.GroupsCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupsCustomersRepository extends JpaRepository<GroupsCustomer, Integer> {
}
