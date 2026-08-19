package com.teamflow.security;

import com.teamflow.domain.Role;
import com.teamflow.domain.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    @Test
    void userDetailsMethodsShouldDelegateToUser() {
        User user = User.builder()
            .id(UUID.randomUUID()).username("alice").password("hash")
            .role(Role.USER).enabled(false)
            .build();
        UserPrincipal principal = new UserPrincipal(user);

        assertEquals(user, principal.getUser());
        assertEquals("alice", principal.getUsername());
        assertEquals("hash", principal.getPassword());
        assertFalse(principal.isEnabled());
        assertEquals(1, principal.getAuthorities().size());
        assertEquals("ROLE_USER", principal.getAuthorities().iterator().next().getAuthority());
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
    }
}
