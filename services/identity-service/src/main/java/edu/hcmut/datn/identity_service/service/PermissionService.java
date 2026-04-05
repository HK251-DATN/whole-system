package edu.hcmut.datn.identity_service.service;

import java.util.List;

import edu.hcmut.datn.identity_service.dao.Permission;

public interface PermissionService {
    Permission create(Permission permission);

    Permission get(Long perId);

    List<Permission> getAll(Integer page, Integer pageSize);

    Permission update(Long perId, Permission permission);

    boolean delete(Long perId);

}
