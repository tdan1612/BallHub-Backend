package com.ballhub.ballhub_backend.controller;

import com.ballhub.ballhub_backend.dto.reponse.ApiResponse;
import com.ballhub.ballhub_backend.dto.reponse.user.UserResponse;
import com.ballhub.ballhub_backend.dto.request.user.UpdateProfileRequest;
import com.ballhub.ballhub_backend.entity.User;
import com.ballhub.ballhub_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            User user = userService.getUserByEmail(email);
            UserResponse userResponse = UserResponse.builder()
                    .userId(user.getUserId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    .role(user.getRole())
                    .build();

            return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin thành công", userResponse));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // ==========================================
    // 1. API CẬP NHẬT TÊN VÀ SĐT
    // ==========================================
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            userService.updateProfile(email, request.getFullName(), request.getPhone());
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // ==========================================
    // 2. API UPLOAD AVATAR
    // ==========================================
    @PostMapping("/me/avatar")
    public ResponseEntity<?> updateAvatar(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            String avatarUrl = userService.updateAvatar(email, file);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật ảnh thành công", avatarUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}