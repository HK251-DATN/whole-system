package edu.hcmut.datn.identity_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.hcmut.datn.identity_service.dao.Group;
import edu.hcmut.datn.identity_service.dao.GroupPermission;
import edu.hcmut.datn.identity_service.dao.Permission;
import edu.hcmut.datn.identity_service.dao.User;
import edu.hcmut.datn.identity_service.dto.misc.PermissionBasicView;
import edu.hcmut.datn.identity_service.dto.misc.UserBasicView;
import edu.hcmut.datn.identity_service.dto.request.GroupPermissionDTO;
import edu.hcmut.datn.identity_service.dto.request.GroupRequest;
import edu.hcmut.datn.identity_service.dto.request.UserGroupDTO;
import edu.hcmut.datn.identity_service.dto.response.ApiResponse;
import edu.hcmut.datn.identity_service.service.GroupService;

@RestController
@PreAuthorize("hasAuthority('GROUP_MANAGE')")
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<Group>> create(@RequestBody GroupRequest groupRequest) {
        Group createResult = groupService.create(groupRequest.toEntity());

        if (createResult == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Create group failed", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create group success", createResult));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Group>>> getAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<Group> results = groupService.getAll(page, pageSize);

        if (results.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No group found", null));
        }

        return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get groups success", results));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Group>> getGroup(@PathVariable Long groupId) {
        Group getResult = groupService.get(groupId);

        if (getResult == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Group not found", null));
        }

        return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Found group", getResult));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Group>> update(
            @PathVariable Long groupId,
            @RequestBody GroupRequest groupRequest) {

        Group updateResult = groupService.update(groupId, groupRequest.toEntity());

        if (updateResult == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Update failed", null));
        }

        return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updateResult));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Group>> delete(@PathVariable Long groupId) {
        Boolean deleteResult = groupService.delete(groupId);

        if (!deleteResult) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Delete failed", null));
        }

        return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null));
    }

    @GetMapping("/{groupId}/user")
    public ResponseEntity<ApiResponse<List<UserBasicView>>> getUsers(@PathVariable Long groupId) {
        List<UserBasicView> results = groupService.getUser(groupId);

        if (results.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No users found", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get users in group success", results));
    }

    @PostMapping("/{groupId}/user")
    public ResponseEntity<ApiResponse<List<User>>> addUser(@RequestBody UserGroupDTO userGroupDTO) {
        boolean addUserResult = groupService.addUser(userGroupDTO.getGroupId(), userGroupDTO.getUserId());

        if (!addUserResult) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Add user fail", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Add user success", null));
    }

    @DeleteMapping("/{groupId}/user")
    public ResponseEntity<ApiResponse<List<User>>> removeUser(@RequestBody UserGroupDTO userGroupDTO) {
        boolean removeUserFromGroupResult = groupService.removeUser(userGroupDTO.getGroupId(),
                userGroupDTO.getUserId());

        if (!removeUserFromGroupResult) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Remove user from group fail", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Remove user from group success", null));
    }

    @GetMapping("/{groupId}/permission")
    public ResponseEntity<ApiResponse<List<PermissionBasicView>>> getPermissions(@PathVariable Long groupId) {
        List<PermissionBasicView> results = groupService.getPermission(groupId);

        if (results.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No permissions found", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get permissions of group success", results));
    }

    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @PostMapping("/{groupId}/permission")
    public ResponseEntity<ApiResponse<Permission>> grantPermission(
            @RequestBody GroupPermissionDTO groupPermissionDTO) {

        GroupPermission tempEntity = groupPermissionDTO.toEntity();

        if (!groupService.grantPermission(tempEntity)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Grant permission to group fail",
                            null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Grant permission to group success", null));
    }

    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @DeleteMapping("/{groupId}/permission")
    public ResponseEntity<ApiResponse<Permission>> revokePermission(
            @RequestBody GroupPermissionDTO groupPermissionDTO) {
        // GroupPermission tempEntity = groupPermissionDTO.toEntity();

        if (!groupService.revokePermission(groupPermissionDTO)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Revoke permission of group fail",
                            null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Revoke permission of group success", null));
    }
}
