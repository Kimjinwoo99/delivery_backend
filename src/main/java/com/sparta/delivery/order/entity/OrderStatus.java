package com.sparta.delivery.order.entity;

public enum OrderStatus {
    ORDERED,    // 주문요청
    PAID,       // 결제완료
    ACCEPTED,   // 주문수락
    COMPLETED,  // 배달완료
    CANCELED    // 주문취소
}
