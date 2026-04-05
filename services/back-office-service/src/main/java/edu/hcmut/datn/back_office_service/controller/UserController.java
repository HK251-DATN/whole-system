package edu.hcmut.datn.back_office_service.controller;

import java.util.List;

import edu.hcmut.datn.back_office_service.security.portable.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import edu.hcmut.datn.back_office_service.dao.User;
import edu.hcmut.datn.back_office_service.dto.request.UserDTO;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.R2UploadService;
import edu.hcmut.datn.back_office_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api/user")
@Controller
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final R2UploadService r2UploadService;

    @Value("${app.user-avatar-bucket}")
    private String userAvtBucket;

    @GetMapping
    public ResponseEntity<ApiResponse<User>> readUser(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        try {
            Long userId = principal.getId();
            
            User user = userService.read(userId);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read user successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
    
    // USER ENDPOINTS
    // User update their information
    @PutMapping
    public ResponseEntity<ApiResponse<User>> userUpdate(
            @RequestBody UserDTO userDTO,
            @AuthenticationPrincipal AuthenticatedUser principal)
    {
        try {
            Long userId = principal.getId();
            
            User updatedUser = userService.update(userId, userDTO.toEntity());
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update user successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
    
    // User update their avatar images
    @PostMapping("avt-image")
    public ResponseEntity<ApiResponse<User>> userUploadAvtImg(
            @RequestParam("file") MultipartFile avtImage,
            @AuthenticationPrincipal AuthenticatedUser principal)
    {
        try {
            Long userId = principal.getId();
            
            String avtUrl = r2UploadService.upload(avtImage, userAvtBucket);
            
            User user = userService.updateUserAvatar(userId, avtUrl);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update user avatar success", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
    
    // ADMIN ENDPOINTS
    
    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<User>> create(@RequestBody UserDTO userDTO) {

        try {
            User newUser = userService.create(userDTO.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create user successfully", newUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
    
    @GetMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<User>> read(@PathVariable Long userId) {
        try {
            User user = userService.read(userId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create user successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<User>>> readAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<User> users = userService.readAll(page, pageSize);

        if (users.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No user exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all users successfully", users));
    }

    @PutMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable Long userId, @RequestBody UserDTO userDTO) {
        try {
            User updatedUser = userService.update(userId, userDTO.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update user successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long userId) {
        try {
            userService.delete(userId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete user successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/admin/{userId}/avt-image")
    public ResponseEntity<ApiResponse<User>> uploadAvtImg(@PathVariable Long userId, @RequestParam("file") MultipartFile avtImage) {
        try {
            String avtUrl = r2UploadService.upload(avtImage, userAvtBucket);

            User user = userService.updateUserAvatar(userId, avtUrl);

            // TODO: Remove user old avatar file
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update user avatar success", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
