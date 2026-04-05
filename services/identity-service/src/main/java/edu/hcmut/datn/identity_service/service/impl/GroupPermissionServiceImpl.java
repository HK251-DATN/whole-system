package edu.hcmut.datn.identity_service.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.identity_service.dao.GroupPermission;
import edu.hcmut.datn.identity_service.repository.GroupPermissionRepository;
import edu.hcmut.datn.identity_service.service.GroupPermissionService;

@Service
public class GroupPermissionServiceImpl implements GroupPermissionService {

    @Autowired
    GroupPermissionRepository groupPermissionRepository;

    @Override
    public GroupPermission create(GroupPermission groupPermission) {
        if (groupPermissionRepository.existsByPerIdAndGroupId(groupPermission.getPerId(),
                groupPermission.getGroupId())) {
            // TODO: Log error out
            return null;
        }

        return groupPermissionRepository.save(groupPermission);
    }

    @Override
    public GroupPermission read(Long id) {
        Optional<GroupPermission> readResult = groupPermissionRepository.findById(id);

        if (readResult.isPresent()) {
            return readResult.get();
        }

        return null;
    }

    @Override
    public GroupPermission update(GroupPermission groupPermission) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }
}
