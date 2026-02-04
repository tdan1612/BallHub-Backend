package com.ballhub.ballhub_backend.controller;

import com.ballhub.ballhub_backend.dto.reponse.ApiResponse;
import com.ballhub.ballhub_backend.dto.reponse.user.UserResponse;
import com.ballhub.ballhub_backend.entity.User;
import com.ballhub.ballhub_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                    .role(user.getRole())
                    .build();

            return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin thành công", userResponse));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
