package com.smartparking.identity.repository;

import com.smartparking.identity.entity.RoleFunctionAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleFunctionActionRepository extends JpaRepository<RoleFunctionAction, RoleFunctionAction.RoleFunctionActionId> {
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM role_function_action rfa " +
            "JOIN functions f ON rfa.func_id = f.id " +
            "JOIN actions a ON rfa.action_id = a.id " +
            "WHERE rfa.role_id = :roleId " +
            "AND f.func_name = :funcName " +
            "AND a.action_code = :actionCode",
            nativeQuery = true)
    boolean hasPermission(@Param("roleId") Integer roleId,
                          @Param("funcName") String funcName,
                          @Param("actionCode") String actionCode);
}
