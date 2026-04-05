package edu.hcmut.datn.identity_service.dto.request;

import edu.hcmut.datn.identity_service.dao.Group;
import lombok.Data;

@Data
public class GroupRequest {
    private String groupName;

    private String description;

    private Boolean isActive;

    public Group toEntity() {
        Group grp = new Group();

        grp.setActive(this.isActive);
        grp.setGroupName(this.groupName);
        grp.setDescription(this.description);

        return grp;
    }
}
