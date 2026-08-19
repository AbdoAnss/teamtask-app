package com.teamflow.security;

import com.teamflow.domain.Role;
import com.teamflow.domain.User;
import com.teamflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameShouldReturnPrincipalWithRoleAuthority() {
        User user = User.builder()
            .id(UUID.randomUUID()).username("alice").password("hash").role(Role.ADMIN).enabled(true)
            .build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var principal = (UserPrincipal) userDetailsService.loadUserByUsername("alice");

        assertEquals("alice", principal.getUsername());
        assertEquals("hash", principal.getPassword());
        assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(principal.isEnabled());
    }

    @Test
    void loadUserByUsernameShouldFailForUnknownUser() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("nobody"));
    }
}
