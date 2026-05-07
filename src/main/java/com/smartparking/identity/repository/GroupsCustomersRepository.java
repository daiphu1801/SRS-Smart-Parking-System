package com.smartparking.identity.repository;

import com.smartparking.identity.entity.GroupsCustomer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupsCustomersRepository extends JpaRepository<GroupsCustomer, Integer>, JpaSpecificationExecutor<GroupsCustomer> {

    @Query("SELECT g.id FROM GroupsCustomer g WHERE g.masterAccount.id = :accountId")
    List<Integer> findGroupIdsByMasterAccountId(@Param("accountId") Integer accountId);
}
