package com.sparta.delivery.order.repository;

import com.sparta.delivery.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 손님: 본인이 한 주문. 응답에 메뉴 이름이 들어가므로 메뉴를 함께 가져온다(N+1 방지).
    @EntityGraph(attributePaths = "menu")
    List<Order> findAllByCustomerUsernameOrderByIdAsc(String username);

    // 사장님: 본인 메뉴에 들어온 주문(삭제된 메뉴의 주문 포함)
    @EntityGraph(attributePaths = "menu")
    List<Order> findAllByMenuOwnerUsernameOrderByIdAsc(String username);

    // 결제처럼 "같은 주문에 동시에 두 번 들어오면 안 되는" 요청에서 쓴다.
    // 주문 행에 쓰기 잠금을 걸어, 먼저 온 요청이 끝날 때까지 다른 요청이 기다리게 한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Order> findWithLockById(Long id);
}
