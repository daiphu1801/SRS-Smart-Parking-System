package com.smartparking.identity.repository;

import com.smartparking.identity.entity.RoleFunctionAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleFunctionActionRepository extends JpaRepository<RoleFunctionAction, RoleFunctionAction.RoleFunctionActionId> {
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM role_function_action rfa " +
            "JOIN functions f ON rfa.func_id = f.id " +
            "JOIN actions a ON rfa.action_id = a.id " +
            "WHERE rfa.role_id = :roleId " +
            "AND f.function_code = :functionCode " +
            "AND a.action_code = :actionCode",
            nativeQuery = true)
    boolean hasPermission(@Param("roleId") Integer roleId,
                          @Param("functionCode") String functionCode,
                          @Param("actionCode") String actionCode);

    @Query(value = "SELECT CONCAT(f.function_code, '_', a.action_code) " +
            "FROM role_function_action rfa " +
            "JOIN functions f ON rfa.func_id = f.id " +
            "JOIN actions a ON rfa.action_id = a.id " +
            "WHERE rfa.role_id = :roleId",
            nativeQuery = true)
    List<String> findPermissionCodesByRoleId(@Param("roleId") Integer roleId);

    List<RoleFunctionAction> findByRoleId(Integer roleId);

    @Modifying
    @Query("DELETE FROM RoleFunctionAction rfa WHERE rfa.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Integer roleId);

}
