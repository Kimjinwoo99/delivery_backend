package com.sparta.delivery.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 금액은 요청으로 받지 않는다. 총액은 서버가 계산한다.
public record OrderCreateRequest(
        @NotNull
        Long menuId,

        @NotNull @Min(1)
        Integer quantity,

        @NotBlank @Size(max = 255)
        String deliveryAddress
) {
}
