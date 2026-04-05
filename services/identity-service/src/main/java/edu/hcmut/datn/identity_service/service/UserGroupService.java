package edu.hcmut.datn.identity_service.service;

import edu.hcmut.datn.identity_service.dao.UserGroup;

public interface UserGroupService {

    UserGroup create();

    UserGroup read();

    UserGroup update();

    boolean delete();

}
