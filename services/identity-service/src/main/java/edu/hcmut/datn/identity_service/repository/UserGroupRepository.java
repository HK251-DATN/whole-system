package edu.hcmut.datn.identity_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import edu.hcmut.datn.identity_service.dao.UserGroup;
import edu.hcmut.datn.identity_service.dto.misc.UserBasicView;
import jakarta.transaction.Transactional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
	boolean existsByUserIdAndGroupId(Long userId, Long groupId);

	@Query(value = """
			    SELECT u.user_id AS userId, u.user_email AS userEmail
			    FROM users u
			    JOIN user_group ug ON u.user_id = ug.user_id
			    WHERE ug.group_id = ?1
			        AND ug.is_active = true
			""", nativeQuery = true)
	List<UserBasicView> getUserBelongToGroup(Long groupId);

	@Modifying
	@Transactional
	@Query(value = """
			DELETE from user_group ug
			WHERE ug.user_id = ?1 AND ug.group_id = ?2
			""", nativeQuery = true)
	void deleteUserFromGroup(Long userId, Long groupId);
}
