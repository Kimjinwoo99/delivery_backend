package com.sparta.delivery.order.dto.response;

import com.sparta.delivery.order.entity.Order;
import com.sparta.delivery.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        Long menuId,
        String menuName,
        Long customerId,
        int quantity,
        int totalPrice,
        String deliveryAddress,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getMenu().getId(),
                order.getMenu().getName(),
                order.getCustomer().getId(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
