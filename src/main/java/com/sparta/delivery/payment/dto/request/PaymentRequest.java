package com.sparta.delivery.payment.dto.request;

import com.sparta.delivery.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

// 결제 수단은 CARD 만 받는다(enum 에 없는 값은 400). 금액은 요청으로 받지 않는다.
public record PaymentRequest(
        @NotNull
        PaymentMethod method
) {
}
