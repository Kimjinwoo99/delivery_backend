package com.sparta.delivery.user.dto.response;

import com.sparta.delivery.user.entity.Role;
import com.sparta.delivery.user.entity.User;

import java.time.LocalDateTime;

// 비밀번호는 응답에 담지 않는다.
public record UserResponse(
        Long userId,
        String username,
        Role role,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getCreatedAt());
    }
}
