package com.sparta.delivery.order.entity;

import com.sparta.delivery.global.entity.BaseEntity;
import com.sparta.delivery.menu.entity.Menu;
import com.sparta.delivery.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// order 는 PostgreSQL 예약어라 테이블명을 orders 로 지정한다.
@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문자(손님)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // 총액은 서비스가 (메뉴 가격 x 수량)으로 계산해서 넘긴다. 처음 상태는 항상 ORDERED.
    public Order(User customer, Menu menu, int quantity, int totalPrice, String deliveryAddress) {
        this.customer = customer;
        this.menu = menu;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.deliveryAddress = deliveryAddress;
        this.status = OrderStatus.ORDERED;
    }

    // 상태 전이가 허용되는지는 서비스가 확인하고, 여기서는 값만 바꾼다.
    public void pay() {
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }

    public void accept() {
        this.status = OrderStatus.ACCEPTED;
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public boolean isOrderedBy(String username) {
        return customer.getUsername().equals(username);
    }

    // 이 주문이 들어온 메뉴의 주인(사장님)인지
    public boolean isReceivedBy(String ownerUsername) {
        return menu.isOwnedBy(ownerUsername);
    }
}
