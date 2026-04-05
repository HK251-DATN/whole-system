package edu.hcmut.datn.identity_service.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import edu.hcmut.datn.identity_service.dao.User;
import edu.hcmut.datn.identity_service.dto.misc.GroupBasicView;
import edu.hcmut.datn.identity_service.dto.misc.PermissionBasicView;
import edu.hcmut.datn.identity_service.dto.request.UserRegistrationRequest;

public interface UserService {

    User create(User user);

    User create(UserRegistrationRequest request);

    List<User> getAll(Integer page, Integer pageSize);

    User get(Long id);

    User getByEmail(String email);

    User update(Long id, User user);

    Boolean delete(Long id);

    Boolean authenticate(String email, String rawPassword);

    List<PermissionBasicView> getUserPermissions(Long userId);

    List<GroupBasicView> getUserGroups(Long userId);

    List<String> getUserPermissionsList(Long userId);
    
    void changePassword(Long userId, String oldPassword, String newPassword);
}
