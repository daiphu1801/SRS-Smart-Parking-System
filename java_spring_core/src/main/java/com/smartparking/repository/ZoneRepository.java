package com.smartparking.repository;

import com.smartparking.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {
    List<Zone> findByParentZoneId(Integer parentZoneId);
    List<Zone> findByParentZoneIdIsNull();
}
