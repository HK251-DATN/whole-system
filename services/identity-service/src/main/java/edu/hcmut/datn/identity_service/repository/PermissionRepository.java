package edu.hcmut.datn.identity_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.identity_service.dao.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPerCode(String perCode);

    Boolean existsByPerCode(String perCode);
}
