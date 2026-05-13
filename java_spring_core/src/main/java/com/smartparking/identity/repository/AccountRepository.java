package com.smartparking.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import com.smartparking.identity.entity.Account;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findBySupabaseId(UUID supabaseId);

    boolean existsByUsername(String username);
}
