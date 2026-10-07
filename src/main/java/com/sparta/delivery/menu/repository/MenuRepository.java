package com.sparta.delivery.menu.repository;

import com.sparta.delivery.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
