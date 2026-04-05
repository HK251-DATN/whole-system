package edu.hcmut.datn.identity_service.dto.request;

import edu.hcmut.datn.identity_service.dao.UserGroup;
import lombok.Data;

@Data
public class UserGroupDTO {

    private long userId;

    private long groupId;

    private boolean isActive;

    public UserGroup toEntity() {
        UserGroup userGroup = new UserGroup();

        userGroup.setGroupId(groupId);
        userGroup.setUserId(userId);
        userGroup.setActive(isActive);

        return userGroup;
    }

}
