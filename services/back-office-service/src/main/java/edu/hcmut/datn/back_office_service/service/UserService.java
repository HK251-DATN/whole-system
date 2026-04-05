package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.User;

@Service
public interface UserService {

    User create(User user);

    User read(Long userId);

    List<User> readAll(Integer page, Integer size);

    User update(Long userId, User user);

    void delete(Long userId);

    User updateUserAvatar(Long userId, String avtUrl);

}
