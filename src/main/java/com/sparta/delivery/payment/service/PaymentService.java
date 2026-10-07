package com.sparta.delivery.payment.service;

import com.sparta.delivery.global.security.AuthUser;
import com.sparta.delivery.order.entity.Order;
import com.sparta.delivery.order.entity.OrderStatus;
import com.sparta.delivery.order.repository.OrderRepository;
import com.sparta.delivery.payment.dto.request.PaymentRequest;
import com.sparta.delivery.payment.dto.response.PaymentResponse;
import com.sparta.delivery.payment.entity.Payment;
import com.sparta.delivery.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    // 결제 저장과 주문 상태 변경은 한 트랜잭션이다. 하나라도 실패하면 둘 다 취소된다.
    @Transactional
    public PaymentResponse pay(AuthUser authUser, Long orderId, PaymentRequest request) {
        // 같은 주문에 결제 요청이 동시에 두 번 들어와도 "결제 완료"가 한 번만 생기도록 행 잠금을 건다.
        Order order = orderRepository.findWithLockById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (!order.isOrderedBy(authUser.username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 주문만 결제할 수 있습니다.");
        }
        // 주문요청 상태일 때만 결제된다. 이미 결제됐거나 취소된 주문은 거절한다.
        if (order.getStatus() != OrderStatus.ORDERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "주문요청 상태의 주문만 결제할 수 있습니다.");
        }

        // 결제 금액은 요청이 아니라 주문 총액을 그대로 쓴다.
        Payment payment = paymentRepository.save(new Payment(order, order.getTotalPrice(), request.method()));
        order.pay();

        return PaymentResponse.from(payment);
    }
}
