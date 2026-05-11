package org.example.sudobeats.service;

import org.example.sudobeats.dto.request.CreateUserRequest;
import org.example.sudobeats.dto.response.UserResponse;
import org.example.sudobeats.entity.User;
import org.example.sudobeats.exception.DuplicateResourceException;
import org.example.sudobeats.exception.UserNotFoundException;
import org.example.sudobeats.mapper.UserMapper;
import org.example.sudobeats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("email", request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .build();

        User saved = userRepository.save(user);
        log.info("Created user [{}] with id={}", saved.getUsername(), saved.getId());
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }
}
