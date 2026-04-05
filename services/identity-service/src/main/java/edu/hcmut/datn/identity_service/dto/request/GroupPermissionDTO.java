package edu.hcmut.datn.identity_service.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import edu.hcmut.datn.identity_service.dao.GroupPermission;
import lombok.Data;

@Data
public class GroupPermissionDTO {

    private Long groupId;

    private Long perId;

    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validUntil;

    public GroupPermission toEntity() {
        GroupPermission groupPermission = new GroupPermission();

        groupPermission.setGroupId(groupId);
        groupPermission.setPerId(perId);
        groupPermission.setActive(isActive);
        groupPermission.setValidUntil(validUntil);

        return groupPermission;
    }

}
