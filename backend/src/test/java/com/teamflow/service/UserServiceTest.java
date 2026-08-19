package com.teamflow.service;

import com.teamflow.domain.Role;
import com.teamflow.domain.User;
import com.teamflow.dto.user.UpdateUserRequest;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.exception.ConflictException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.mapper.UserMapperImpl;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void init() {
        userService = new UserService(userRepository, passwordEncoder, new UserMapperImpl());
    }

    private User user(String username, Role role) {
        return User.builder()
            .id(UUID.randomUUID()).username(username).email(username + "@teamflow.test")
            .password("hashed").firstName(username).role(role).enabled(true)
            .build();
    }

    @Test
    void getAllUsersShouldReturnPageOfDtos() {
        User alice = user("alice", Role.USER);
        when(userRepository.findAll(PageRequest.of(0, 20))).thenReturn(new PageImpl<>(List.of(alice)));

        var page = userService.getAllUsers(PageRequest.of(0, 20));

        assertEquals(1, page.getContent().size());
        assertEquals("alice", page.getContent().get(0).getUsername());
        assertEquals(Role.USER, page.getContent().get(0).getRole());
    }

    @Test
    void getUserByIdShouldFailWhenUserMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(id));
    }

    @Test
    void updateUserShouldChangeOwnProfile() {
        User alice = user("alice", Role.USER);
        when(userRepository.findById(alice.getId())).thenReturn(Optional.of(alice));
        when(userRepository.save(any(User.class))).thenReturn(alice);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Alicia");

        var dto = userService.updateUser(alice.getId(), request, new UserPrincipal(alice));

        assertEquals("Alicia", dto.getFirstName());
        assertEquals("Alicia", alice.getFirstName());
    }

    @Test
    void updateUserShouldFailWhenEditingSomeoneElseAsNonAdmin() {
        User alice = user("alice", Role.USER);
        User bob = user("bob", Role.USER);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Bobby");

        assertThrows(AccessDeniedException.class, () ->
            userService.updateUser(bob.getId(), request, new UserPrincipal(alice))
        );
    }

    @Test
    void updateUserShouldAllowAdminToEditAnyone() {
        User admin = user("admin", Role.ADMIN);
        User bob = user("bob", Role.USER);
        when(userRepository.findById(bob.getId())).thenReturn(Optional.of(bob));
        when(userRepository.save(any(User.class))).thenReturn(bob);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Bobby");

        var dto = userService.updateUser(bob.getId(), request, new UserPrincipal(admin));

        assertEquals("Bobby", dto.getFirstName());
    }

    @Test
    void updateUserShouldRejectEmailAlreadyInUse() {
        User alice = user("alice", Role.USER);
        when(userRepository.findById(alice.getId())).thenReturn(Optional.of(alice));
        when(userRepository.existsByEmail("taken@teamflow.test")).thenReturn(true);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("taken@teamflow.test");

        assertThrows(ConflictException.class, () ->
            userService.updateUser(alice.getId(), request, new UserPrincipal(alice))
        );
    }

    @Test
    void updateUserShouldEncodeNewPassword() {
        User alice = user("alice", Role.USER);
        when(userRepository.findById(alice.getId())).thenReturn(Optional.of(alice));
        when(userRepository.save(any(User.class))).thenReturn(alice);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("new-password");

        userService.updateUser(alice.getId(), request, new UserPrincipal(alice));

        assertEquals("encoded", alice.getPassword());
    }

    @Test
    void deleteUserShouldFailForNonAdmin() {
        User alice = user("alice", Role.USER);
        assertThrows(AccessDeniedException.class, () ->
            userService.deleteUser(UUID.randomUUID(), new UserPrincipal(alice))
        );
    }

    @Test
    void deleteUserShouldDeleteForAdmin() {
        User admin = user("admin", Role.ADMIN);
        User bob = user("bob", Role.USER);
        when(userRepository.findById(bob.getId())).thenReturn(Optional.of(bob));

        userService.deleteUser(bob.getId(), new UserPrincipal(admin));

        verify(userRepository).deleteById(bob.getId());
    }
}
