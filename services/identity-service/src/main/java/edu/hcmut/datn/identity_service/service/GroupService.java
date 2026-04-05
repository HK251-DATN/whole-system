package edu.hcmut.datn.identity_service.service;

import java.util.List;

import edu.hcmut.datn.identity_service.dao.Group;
import edu.hcmut.datn.identity_service.dao.GroupPermission;
import edu.hcmut.datn.identity_service.dto.misc.PermissionBasicView;
import edu.hcmut.datn.identity_service.dto.misc.UserBasicView;
import edu.hcmut.datn.identity_service.dto.request.GroupPermissionDTO;

public interface GroupService {

    Group create(Group group);

    Group get(Long id);

    List<Group> getAll(Integer page, Integer pageSize);

    Group update(Long id, Group group);

    boolean delete(Long id);

    boolean addUser(Long groupId, Long userId);

    List<UserBasicView> getUser(Long groupId);

    boolean removeUser(Long groupId, Long userId);

    boolean grantPermission(GroupPermission groupPermission);

    List<PermissionBasicView> getPermission(Long groupId);

    boolean revokePermission(GroupPermission groupPermission);

    boolean revokePermission(GroupPermissionDTO groupPermissionDTO);
}
