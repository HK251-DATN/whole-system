package edu.hcmut.datn.identity_service.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.identity_service.dao.Permission;
import edu.hcmut.datn.identity_service.repository.PermissionRepository;
import edu.hcmut.datn.identity_service.service.PermissionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Permission create(Permission permission) {
        try {
            if (permissionRepository.existsByPerCode(permission.getPerCode())) {
                throw new RuntimeException("Duplicated permission");
            } else {
                return permissionRepository.save(permission);
            }
        } catch (Exception e) {
            // TODO: Log the exception
            return null;
        }
    }

    @Override
    public Permission get(Long perId) {
        try {
            if (permissionRepository.findById(perId).isEmpty()) {
                throw new RuntimeException("Permission not found");
            } else {
                return permissionRepository.findById(perId).get();
            }
        } catch (Exception e) {
            // TODO: Log the exception
            return null;
        }
    }

    @Override
    public List<Permission> getAll(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Permission> permissionPage = permissionRepository.findAll(pageable);
        return permissionPage.getContent();
    }

    @Override
    public Permission update(Long perId, Permission permission) {
        try {
            Optional<Permission> curPermission = permissionRepository.findById(perId);
            if (curPermission.isPresent()) {
                Permission curPermissionInstance = curPermission.get();
                curPermissionInstance.setActive(permission.isActive());
                curPermissionInstance.setPerCode(permission.getPerCode());
                curPermissionInstance.setPerDescription(permission.getPerDescription());
                curPermissionInstance.setPerName(permission.getPerName());
                return permissionRepository.save(curPermission.get());
            } else {
                throw new RuntimeException("Permission not found");
            }
        } catch (Exception e) {
            // TODO: Log the exception
            return null;
        }
    }

    @Override
    public boolean delete(Long perId) {
        try {
            permissionRepository.findById(perId).ifPresent(permission -> permissionRepository.delete(permission));

            return true;
        } catch (Exception e) {
            // TODO: Log the exception
            return false;
        }
    }
}
