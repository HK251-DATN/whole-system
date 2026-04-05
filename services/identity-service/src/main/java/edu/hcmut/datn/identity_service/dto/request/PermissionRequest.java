package edu.hcmut.datn.identity_service.dto.request;

import edu.hcmut.datn.identity_service.dao.Permission;
import lombok.Data;

@Data
public class PermissionRequest {
    private String perCode;
    private String perName;
    private String perDescription;
    private Boolean isActive;

    public Permission toEntity() {
        Permission permission = new Permission();

        permission.setActive(this.isActive);
        permission.setPerName(this.perName);
        permission.setPerCode(this.perCode);
        permission.setPerDescription(this.perDescription);

        return permission;
    }
}
