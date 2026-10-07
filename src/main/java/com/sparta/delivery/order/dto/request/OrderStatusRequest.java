package com.sparta.delivery.order.dto.request;

import com.sparta.delivery.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

// 사장님이 바꿀 수 있는 값은 ACCEPTED, COMPLETED 뿐이다. 그 외 값은 서비스에서 400 으로 거절한다.
public record OrderStatusRequest(
        @NotNull
        OrderStatus status
) {
}
