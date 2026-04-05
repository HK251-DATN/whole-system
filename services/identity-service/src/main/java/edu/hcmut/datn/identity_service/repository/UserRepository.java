package edu.hcmut.datn.identity_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.hcmut.datn.identity_service.dao.User;
import edu.hcmut.datn.identity_service.dto.misc.GroupBasicView;
import edu.hcmut.datn.identity_service.dto.misc.PermissionBasicView;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserEmail(String email);

    boolean existsByUserEmail(String email);

    @Query(value = """
            SELECT DISTINCT p.per_id, p.per_code, p.per_name, p.per_des
            FROM user_group ug
            JOIN group_permission gp
              ON ug.group_id = gp.group_id
            JOIN permissions p
              ON gp.per_id = p.per_id
            WHERE ug.user_id = ?1
              AND ug.is_active = true
              AND gp.is_active = true
              AND p.is_active = true
            """, nativeQuery = true)
    List<PermissionBasicView> getUserPermissions(Long userId);

    @Query(value = """
            SELECT g.group_id, g.group_name, g.group_des
            FROM user_group ug
            JOIN groups g
              ON ug.group_id = g.group_id
            WHERE ug.user_id = ?1
              AND g.is_active = true
              AND ug.is_active = true
            """, nativeQuery = true)
    List<GroupBasicView> getUserGroups(Long userId);
}
