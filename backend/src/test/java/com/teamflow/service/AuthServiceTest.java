package com.teamflow.service;

import com.teamflow.domain.RefreshToken;
import com.teamflow.domain.User;
import com.teamflow.dto.auth.LoginRequest;
import com.teamflow.dto.auth.RegisterRequest;
import com.teamflow.repository.RefreshTokenRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.JwtTokenProvider;
import com.teamflow.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604800000L);
    }

    @Test
    void registerShouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@teamflow.test");
        request.setPassword("password123");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@teamflow.test")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(jwtTokenProvider.generateToken("alice")).thenReturn("access-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);

        User saved = User.builder()
            .id(UUID.randomUUID())
            .username("alice")
            .email("alice@teamflow.test")
            .password("hashed")
            .build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        var response = authService.register(request);

        assertEquals("access-token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void loginShouldAuthenticateAndReturnTokens() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .username("bob")
            .email("bob@teamflow.test")
            .password("pwd")
            .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(new UserPrincipal(user), null);

        LoginRequest request = new LoginRequest();
        request.setUsername("bob");
        request.setPassword("secret");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken("bob")).thenReturn("jwt");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);

        var response = authService.login(request);

        assertEquals("jwt", response.getAccessToken());
        assertEquals("bob", response.getUser().getUsername());
    }

    @Test
    void refreshTokenShouldRotateToken() {
        User user = User.builder().id(UUID.randomUUID()).username("eve").email("eve@test").build();
        RefreshToken refreshToken = RefreshToken.builder()
            .token("old-token")
            .user(user)
            .expiresAt(Instant.now().plusSeconds(120))
            .build();

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.generateToken("eve")).thenReturn("new-access");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);

        var req = new com.teamflow.dto.auth.RefreshTokenRequest();
        req.setRefreshToken("old-token");

        var response = authService.refreshToken(req);

        assertEquals("new-access", response.getAccessToken());
        verify(refreshTokenRepository).delete(refreshToken);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertNotEquals("old-token", captor.getValue().getToken());
    }
}
