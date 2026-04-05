package edu.hcmut.datn.identity_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.hcmut.datn.identity_service.dao.GroupPermission;
import edu.hcmut.datn.identity_service.dto.misc.PermissionBasicView;
import jakarta.transaction.Transactional;

public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long> {
    boolean existsByPerIdAndGroupId(Long perId, Long groupId);

    @Query(value = """
            select
                permissions.per_id,
                permissions.per_code,
                permissions.per_des,
                permissions.per_name
            from group_permission, permissions
            where
            	group_permission.group_id = ?1
            	AND group_permission.per_id = permissions.per_id
            	AND permissions.is_active = true
            	AND group_permission.is_active = true
            	AND group_permission.valid_until > CURRENT_TIMESTAMP;
            """, nativeQuery = true)
    List<PermissionBasicView> getGroupPermissions(Long groupId);

    @Transactional
    void deleteByPerIdAndGroupId(Long perId, Long groupId);
}
