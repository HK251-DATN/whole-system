package edu.hcmut.datn.identity_service.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import edu.hcmut.datn.identity_service.dao.User;
import edu.hcmut.datn.identity_service.dto.misc.GroupBasicView;
import edu.hcmut.datn.identity_service.dto.misc.PermissionBasicView;
import edu.hcmut.datn.identity_service.dto.request.UserRegistrationRequest;
import edu.hcmut.datn.identity_service.messaging.user.UserCreatedEvent;
import edu.hcmut.datn.identity_service.messaging.user.UserEventProducer;
import edu.hcmut.datn.identity_service.repository.UserRepository;
import edu.hcmut.datn.identity_service.service.R2UploadService;
import edu.hcmut.datn.identity_service.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final R2UploadService r2UploadService;

    @PersistenceContext
    private final EntityManager entityManager;

    private final UserEventProducer userEventProducer;

    @Override
    @Transactional
    public User create(User user) {
        try {
            Optional<User> curUser = userRepository.findByUserEmail(user.getUserEmail());
            if (curUser.isPresent()) {
                throw new RuntimeException("Duplicated user");
            } else {
                user.setHashedPwd(passwordEncoder.encode(user.getHashedPwd()));
                return userRepository.save(user);
            }
        } catch (Exception e) {
            // TODO: Log the exception
            return null;
        }
    }

    @Override
    public User create(UserRegistrationRequest request) {
        User newUser = create(request.toUserRequest().toEntity());
        
        String avtUrl = "https://pub-954e99f131cf4cc896de1ad360338682.r2.dev/128c271e-c0a6-433e-bcd1-f3bbc4243401-default-user-avt.png";
        
        if (newUser != null) {
            UserCreatedEvent event = new UserCreatedEvent(
                    newUser.getUserId(),
                    request.getEmail(),
                    request.getFName(),
                    request.getLName(),
                    avtUrl,
                    request.getDob(),
                    request.getPNum(),
                    request.getGender()
            );

            userEventProducer.publishUserCreated(event);
        }

        return newUser;
    }

    @Override
    public List<User> getAll(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.getContent();
    }

    @Override
    public User get(Long id) {
        try {
            Optional<User> curUser = userRepository.findById(id);
            if (curUser.isPresent()) {
                return curUser.get();
            } else {
                throw new RuntimeException("User not found");
            }
        } catch (Exception e) {
            // TODO: Log the exception
            return null;
        }
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByUserEmail(email).orElse(null);
    }

    @Override
    public User update(Long id, User user) {
        try {
            Optional<User> curUser = userRepository.findById(id);
            if (curUser.isPresent()) {
                curUser.get().setUserEmail(user.getUserEmail());
                curUser.get().setHashedPwd(passwordEncoder.encode(user.getHashedPwd()));
                return userRepository.save(curUser.get());
            } else {
                throw new RuntimeException("User not found");
            }
        } catch (Exception e) {
            // TODO: Log the exception
            return null;
        }
    }

    @Override
    public Boolean delete(Long id) {
        try {
            userRepository.findById(id).ifPresent(user -> userRepository.delete(user));

            return true;
        } catch (Exception e) {
            // TODO: Log the exception
            return false;
        }
    }

    @Override
    public Boolean authenticate(String email, String rawPassword) {
        if (!userRepository.existsByUserEmail(email)) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, userRepository.findByUserEmail(email).get().getHashedPwd());
    }

    @Override
    public List<PermissionBasicView> getUserPermissions(Long userId) {
        return userRepository.getUserPermissions(userId);
    }

    @Override
    public List<GroupBasicView> getUserGroups(Long userId) {
        return userRepository.getUserGroups(userId);
    }

    @Override
    public List<String> getUserPermissionsList(Long userId) {
        List<PermissionBasicView> permissionBasics = getUserPermissions(userId);

        List<String> results = new ArrayList<>();

        for (int idx = 0; idx < permissionBasics.size(); idx++) {
            results.add(permissionBasics.get(idx).getPerCode());
        }

        return results;
    }
    
    @Override
    @Transactional
    public void changePassword (Long userId, String oldPassword, String newPassword) {
        boolean isCorrectPassword = true;
        try {
            User user = get(userId);
            
            isCorrectPassword = authenticate(user.getUserEmail(), oldPassword);
            
            if (!isCorrectPassword) {
                throw new RuntimeException();
            }
            
            user.setHashedPwd(passwordEncoder.encode(newPassword));
            
            userRepository.save(user);
        } catch (Exception e) {
            if (!isCorrectPassword)
                throw new RuntimeException("The old password you provided is incorrect!");
            else
                throw new RuntimeException("Unexpected error!");
        }
    }
}
