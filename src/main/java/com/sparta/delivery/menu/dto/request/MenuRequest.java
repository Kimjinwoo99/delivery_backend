package com.sparta.delivery.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 등록과 수정이 같은 값 검증을 쓴다.
public record MenuRequest(
        @NotBlank @Size(max = 100)
        String name,

        @NotNull @Min(1)
        Integer price,

        @Size(max = 500)
        String description
) {
}
