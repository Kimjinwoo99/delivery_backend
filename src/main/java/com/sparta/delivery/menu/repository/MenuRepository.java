package com.sparta.delivery.menu.repository;

import com.sparta.delivery.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 삭제된 메뉴(deletedAt 이 있는 메뉴)는 조회 대상에서 뺀다.
    List<Menu> findAllByDeletedAtIsNullOrderByIdAsc();

    Optional<Menu> findByIdAndDeletedAtIsNull(Long id);
}
