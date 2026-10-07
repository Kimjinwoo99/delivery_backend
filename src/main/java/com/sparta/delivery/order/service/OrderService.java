package com.sparta.delivery.order.service;

import com.sparta.delivery.global.security.AuthUser;
import com.sparta.delivery.menu.entity.Menu;
import com.sparta.delivery.menu.repository.MenuRepository;
import com.sparta.delivery.order.dto.request.OrderCreateRequest;
import com.sparta.delivery.order.dto.request.OrderStatusRequest;
import com.sparta.delivery.order.dto.response.OrderResponse;
import com.sparta.delivery.order.entity.Order;
import com.sparta.delivery.order.entity.OrderStatus;
import com.sparta.delivery.order.repository.OrderRepository;
import com.sparta.delivery.user.entity.Role;
import com.sparta.delivery.user.entity.User;
import com.sparta.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse create(AuthUser authUser, OrderCreateRequest request) {
        User customer = userRepository.findByUsername(authUser.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));

        // 삭제된 메뉴는 없는 메뉴처럼 404
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(request.menuId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."));

        // 총액 = 메뉴 가격 x 수량. 서버가 DB 의 메뉴 가격으로 계산한다.
        long totalPrice = (long) menu.getPrice() * request.quantity();
        if (totalPrice > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문 금액이 너무 큽니다.");
        }

        Order order = orderRepository.save(
                new Order(customer, menu, request.quantity(), (int) totalPrice, request.deliveryAddress()));
        return OrderResponse.from(order);
    }

    public List<OrderResponse> getOrders(AuthUser authUser) {
        List<Order> orders = authUser.role() == Role.OWNER
                ? orderRepository.findAllByMenuOwnerUsernameOrderByIdAsc(authUser.username())
                : orderRepository.findAllByCustomerUsernameOrderByIdAsc(authUser.username());

        return orders.stream().map(OrderResponse::from).toList();
    }

    @Transactional
    public void cancel(AuthUser authUser, Long orderId) {
        Order order = findOrder(orderId);
        if (!order.isOrderedBy(authUser.username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 주문만 취소할 수 있습니다.");
        }
        // 결제 전(주문요청)일 때만 취소할 수 있다.
        if (order.getStatus() != OrderStatus.ORDERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "주문요청 상태의 주문만 취소할 수 있습니다.");
        }
        order.cancel();
    }

    @Transactional
    public void changeStatus(AuthUser authUser, Long orderId, OrderStatusRequest request) {
        OrderStatus target = request.status();
        if (target != OrderStatus.ACCEPTED && target != OrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상태는 ACCEPTED 또는 COMPLETED 로만 변경할 수 있습니다.");
        }

        Order order = findOrder(orderId);
        if (!order.isReceivedBy(authUser.username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 메뉴에 들어온 주문만 변경할 수 있습니다.");
        }

        // 허용되는 전이: 결제완료 -> 주문수락, 주문수락 -> 배달완료
        if (target == OrderStatus.ACCEPTED && order.getStatus() == OrderStatus.PAID) {
            order.accept();
        } else if (target == OrderStatus.COMPLETED && order.getStatus() == OrderStatus.ACCEPTED) {
            order.complete();
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "현재 상태(" + order.getStatus() + ")에서는 " + target + " 로 변경할 수 없습니다.");
        }
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
    }
}
