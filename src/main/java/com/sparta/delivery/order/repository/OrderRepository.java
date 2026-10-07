package com.sparta.delivery.order.repository;

import com.sparta.delivery.order.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 손님: 본인이 한 주문. 응답에 메뉴 이름이 들어가므로 메뉴를 함께 가져온다(N+1 방지).
    @EntityGraph(attributePaths = "menu")
    List<Order> findAllByCustomerUsernameOrderByIdAsc(String username);

    // 사장님: 본인 메뉴에 들어온 주문(삭제된 메뉴의 주문 포함)
    @EntityGraph(attributePaths = "menu")
    List<Order> findAllByMenuOwnerUsernameOrderByIdAsc(String username);
}
