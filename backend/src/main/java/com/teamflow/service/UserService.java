package com.teamflow.service;

import com.teamflow.domain.Role;
import com.teamflow.domain.User;
import com.teamflow.dto.PageResponse;
import com.teamflow.dto.user.UpdateUserRequest;
import com.teamflow.dto.user.UserDto;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.exception.ConflictException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.mapper.UserMapper;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public PageResponse<UserDto> getAllUsers(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toDto));
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        return userMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest request, UserPrincipal principal) {
        User current = principal.getUser();
        if (!current.getId().equals(id) && current.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You can only update your own profile");
        }
        User user = findOrThrow(id);
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (StringUtils.hasText(request.getLastName()))  user.setLastName(request.getLastName());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id, UserPrincipal principal) {
        if (principal.getUser().getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can delete users");
        }
        findOrThrow(id);
        userRepository.deleteById(id);
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
