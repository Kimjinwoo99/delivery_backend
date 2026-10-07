package com.sparta.delivery.payment.dto.response;

import com.sparta.delivery.payment.entity.Payment;
import com.sparta.delivery.payment.entity.PaymentMethod;
import com.sparta.delivery.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        int amount,
        PaymentMethod method,
        PaymentStatus status,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getCreatedAt());
    }
}
