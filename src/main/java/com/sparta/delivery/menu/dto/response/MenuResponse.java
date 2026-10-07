package com.sparta.delivery.menu.dto.response;

import com.sparta.delivery.menu.entity.Menu;

import java.time.LocalDateTime;

public record MenuResponse(
        Long menuId,
        Long ownerId,
        String name,
        int price,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getOwner().getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getCreatedAt(),
                menu.getUpdatedAt());
    }
}
