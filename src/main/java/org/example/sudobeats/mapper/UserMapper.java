package org.example.sudobeats.mapper;

import org.example.sudobeats.dto.response.UserResponse;
import org.example.sudobeats.entity.User;
import org.springframework.stereotype.Component;

/**
 * Manual mapper: User entity → UserResponse DTO.
 * Kept explicit so every exposed field is a conscious decision.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
