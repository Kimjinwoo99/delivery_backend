package com.sparta.delivery.order.controller;

import com.sparta.delivery.global.security.AuthUser;
import com.sparta.delivery.order.dto.request.OrderCreateRequest;
import com.sparta.delivery.order.dto.request.OrderStatusRequest;
import com.sparta.delivery.order.dto.response.OrderResponse;
import com.sparta.delivery.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@AuthenticationPrincipal AuthUser authUser,
                                @Valid @RequestBody OrderCreateRequest request) {
        return orderService.create(authUser, request);
    }

    @GetMapping
    public List<OrderResponse> getOrders(@AuthenticationPrincipal AuthUser authUser) {
        return orderService.getOrders(authUser);
    }

    @PatchMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long orderId) {
        orderService.cancel(authUser, orderId);
    }

    @PatchMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(@AuthenticationPrincipal AuthUser authUser,
                             @PathVariable Long orderId,
                             @Valid @RequestBody OrderStatusRequest request) {
        orderService.changeStatus(authUser, orderId, request);
    }
}
