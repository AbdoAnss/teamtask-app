package com.teamflow.service;

import com.teamflow.domain.RefreshToken;
import com.teamflow.domain.User;
import com.teamflow.dto.auth.*;
import com.teamflow.exception.ConflictException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.repository.RefreshTokenRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.JwtTokenProvider;
import com.teamflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .build();
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = ((UserPrincipal) auth.getPrincipal()).getUser();
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new ResourceNotFoundException("Refresh token expired");
        }
        refreshTokenRepository.delete(storedToken);
        return buildAuthResponse(storedToken.getUser());
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username)
            .ifPresent(user -> refreshTokenRepository.deleteByUserId(user.getId()));
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateToken(user.getUsername());
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
            .token(refreshTokenValue)
            .user(user)
            .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
            .build();
        refreshTokenRepository.save(refreshToken);
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshTokenValue)
            .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build())
            .build();
    }
}
