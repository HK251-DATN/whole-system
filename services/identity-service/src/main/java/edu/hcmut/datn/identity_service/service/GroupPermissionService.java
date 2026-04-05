package edu.hcmut.datn.identity_service.service;

import edu.hcmut.datn.identity_service.dao.GroupPermission;

public interface GroupPermissionService {
    GroupPermission create(GroupPermission groupPermission);
    
    GroupPermission read(Long id);
    
    GroupPermission update(GroupPermission groupPermission);
    
    boolean delete(Long id);
}
