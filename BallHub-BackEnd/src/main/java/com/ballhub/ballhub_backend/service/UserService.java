package com.ballhub.ballhub_backend.service;


import com.ballhub.ballhub_backend.entity.User;
import com.ballhub.ballhub_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndStatusTrue(email)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại hoặc đã bị khóa"));
    }

}
