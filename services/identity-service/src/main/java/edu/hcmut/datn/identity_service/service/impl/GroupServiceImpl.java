package edu.hcmut.datn.identity_service.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.identity_service.dao.Group;
import edu.hcmut.datn.identity_service.dao.GroupPermission;
import edu.hcmut.datn.identity_service.dao.UserGroup;
import edu.hcmut.datn.identity_service.dto.misc.PermissionBasicView;
import edu.hcmut.datn.identity_service.dto.misc.UserBasicView;
import edu.hcmut.datn.identity_service.dto.request.GroupPermissionDTO;
import edu.hcmut.datn.identity_service.repository.GroupPermissionRepository;
import edu.hcmut.datn.identity_service.repository.GroupRepository;
import edu.hcmut.datn.identity_service.repository.UserGroupRepository;
import edu.hcmut.datn.identity_service.service.GroupService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private GroupPermissionRepository groupPermissionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Group create(Group group) {
        try {
            Optional<Group> curGroup = groupRepository.findByGroupName(group.getGroupName());

            if (curGroup.isPresent()) {
                throw new RuntimeException("Duplicated group");
            } else {
                return groupRepository.save(group);
            }
        } catch (Exception e) {
            // TODO: Log the exception
            return null;
        }
    }

    @Override
    public Group get(Long id) {
        try {
            Optional<Group> curGroup = groupRepository.findById(id);
            if (curGroup.isPresent()) {
                return curGroup.get();
            } else {
                throw new RuntimeException("Group not found");
            }
        } catch (Exception e) {
            // TODO: Log the exception
            return null;
        }
    }

    @Override
    public List<Group> getAll(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Group> groupPage = groupRepository.findAll(pageable);
        return groupPage.getContent();
    }

    @Override
    public Group update(Long id, Group group) {
        try {
            Optional<Group> curGroup = groupRepository.findById(id);
            if (curGroup.isPresent()) {
                Group curGroupInstance = curGroup.get();
                curGroupInstance.setGroupName(group.getGroupName());
                curGroupInstance.setDescription(group.getDescription());
                curGroupInstance.setActive(group.isActive());
                return groupRepository.save(curGroupInstance);
            } else {
                throw new RuntimeException("User not found");
            }
        } catch (Exception e) {
            // TODO: Log the exception
            return null;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            groupRepository.findById(id).ifPresent(group -> groupRepository.delete(group));

            return true;
        } catch (Exception e) {
            // TODO: Log the exception
            return false;
        }
    }

    @Override
    public boolean addUser(Long groupId, Long userId) {
        if (userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            return false;
        }

        UserGroup newUserGroup = new UserGroup();

        newUserGroup.setUserId(userId);
        newUserGroup.setGroupId(groupId);
        newUserGroup.setActive(true);

        userGroupRepository.save(newUserGroup);

        return true;
    }

    @Override
    public List<UserBasicView> getUser(Long groupId) {
        return userGroupRepository.getUserBelongToGroup(groupId);
    }

    @Override
    public boolean removeUser(Long groupId, Long userId) {
        if (userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            userGroupRepository.deleteUserFromGroup(userId, groupId);
            return true;
        }

        // TODO: Log error
        return false;
    }

    @Override
    public boolean grantPermission(GroupPermission groupPermission) {
        if (groupPermissionRepository.existsByPerIdAndGroupId(groupPermission.getPerId(),
                groupPermission.getGroupId())) {
            // TODO: Log error
            return false;
        }

        groupPermissionRepository.save(groupPermission);
        return true;
    }

    @Override
    public List<PermissionBasicView> getPermission(Long groupId) {
        return groupPermissionRepository.getGroupPermissions(groupId);
    }

    @Override
    public boolean revokePermission(GroupPermission groupPermission) {
        if (!groupPermissionRepository.existsByPerIdAndGroupId(groupPermission.getPerId(),
                groupPermission.getGroupId())) {
            // TODO: Log error
            return false;
        }

        groupPermissionRepository.deleteByPerIdAndGroupId(groupPermission.getPerId(),
                groupPermission.getGroupId());
        return true;
    }

    @Override
    public boolean revokePermission(GroupPermissionDTO groupPermissionDTO) {
        if (!groupPermissionRepository.existsByPerIdAndGroupId(groupPermissionDTO.getPerId(),
                groupPermissionDTO.getGroupId())) {
            // TODO: Log error
            return false;
        }

        groupPermissionRepository.deleteByPerIdAndGroupId(groupPermissionDTO.getPerId(),
                groupPermissionDTO.getGroupId());
        return true;
    }
}
