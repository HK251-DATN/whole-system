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
import edu.hcmut.datn.identity_service.dao.Permission;
import edu.hcmut.datn.identity_service.dao.User;
import edu.hcmut.datn.identity_service.dto.request.PermissionRequest;
import edu.hcmut.datn.identity_service.dto.response.ApiResponse;
import edu.hcmut.datn.identity_service.service.PermissionService;

@RestController
@PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
@RequestMapping("/api/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Permission>> create(@RequestBody PermissionRequest permissionRequest) {
        Permission createResult = permissionService.create(permissionRequest.toEntity());

        if (createResult == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Create permission failed", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create permission success", createResult));
    }

    @GetMapping("/{perId}")
    public ResponseEntity<ApiResponse<Permission>> get(@PathVariable Long perId) {
        Permission getResult = permissionService.get(perId);

        if (getResult == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Permission not found", null));
        }

        return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Found permission", getResult));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Permission>>> getAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<Permission> results = permissionService.getAll(page, pageSize);

        if (results.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No permission found", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get permissions success", results));
    }

    @PutMapping("/{perId}")
    public ResponseEntity<ApiResponse<Permission>> update(
            @PathVariable Long perId,
            @RequestBody PermissionRequest permissionRequest) {
        Permission updateResult = permissionService.update(perId, permissionRequest.toEntity());

        if (updateResult == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Update failed", null));
        }

        return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updateResult));
    }

    @DeleteMapping("/{perId}")
    public ResponseEntity<ApiResponse<Permission>> delete(@PathVariable Long perId) {
        Boolean deleteResult = permissionService.delete(perId);

        if (!deleteResult) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "Delete failed", null));
        }

        return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null));
    }

    public ResponseEntity<ApiResponse<List<Group>>> getGroups() {
        return null;
    }

    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        return null;
    }
}
