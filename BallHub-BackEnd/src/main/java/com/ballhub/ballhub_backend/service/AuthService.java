package com.ballhub.ballhub_backend.service;

import com.ballhub.ballhub_backend.dto.reponse.auth.AuthResponse;
import com.ballhub.ballhub_backend.dto.reponse.auth.UserResponse;
import com.ballhub.ballhub_backend.dto.request.auth.LoginRequest;
import com.ballhub.ballhub_backend.dto.request.auth.RefreshTokenRequest;
import com.ballhub.ballhub_backend.dto.request.auth.RegisterRequest;
import com.ballhub.ballhub_backend.entity.RefreshToken;
import com.ballhub.ballhub_backend.entity.User;
import com.ballhub.ballhub_backend.exception.UnauthorizedException;
import com.ballhub.ballhub_backend.repository.RefreshTokenRepository;
import com.ballhub.ballhub_backend.repository.UserRepository;
import com.ballhub.ballhub_backend.security.CustomUserDetails;
import com.ballhub.ballhub_backend.security.JwtTokenProvider;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public AuthResponse register(RegisterRequest request) throws BadRequestException {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        // Create new user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role("CUSTOMER")
                .status(true)
                .build();

        User savedUser = userRepository.save(user);

        // Authenticate and generate tokens
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // Save refresh token
        saveRefreshToken(savedUser, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // Get user
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Save refresh token
        saveRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token không hợp lệ");
        }

        // Get refresh token from database
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token không tồn tại hoặc đã bị thu hồi"));

        // Check if expired
        if (storedToken.isExpired()) {
            throw new UnauthorizedException("Refresh token đã hết hạn");
        }

        // Get user and create new authentication
        User user = storedToken.getUser();
        CustomUserDetails userDetails = CustomUserDetails.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        // Generate new access token
        String newAccessToken = tokenProvider.generateAccessToken(authentication);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token không tồn tại"));

        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiredAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
