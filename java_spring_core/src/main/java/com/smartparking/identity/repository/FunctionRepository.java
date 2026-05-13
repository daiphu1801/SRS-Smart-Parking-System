package com.smartparking.identity.repository;

import com.smartparking.identity.entity.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Integer> {
}
