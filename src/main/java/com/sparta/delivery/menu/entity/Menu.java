package com.sparta.delivery.menu.entity;

import com.sparta.delivery.global.entity.BaseEntity;
import com.sparta.delivery.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메뉴 주인(사장님)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(length = 500)
    private String description;

    // Soft Delete: null 이면 정상, 값이 있으면 삭제된 메뉴
    private LocalDateTime deletedAt;

    public Menu(User owner, String name, int price, String description) {
        this.owner = owner;
        this.name = name;
        this.price = price;
        this.description = description;
    }
}
