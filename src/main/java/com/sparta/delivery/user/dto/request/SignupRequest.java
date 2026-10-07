package com.sparta.delivery.user.dto.request;

import com.sparta.delivery.user.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(min = 4, max = 20)
        String username,

        // BCrypt 는 72바이트까지만 해시에 반영하므로 상한을 둔다.
        @NotBlank @Size(min = 8, max = 72)
        String password,

        @NotNull
        Role role
) {
}
