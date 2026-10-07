package com.sparta.delivery.menu.controller;

import com.sparta.delivery.global.security.AuthUser;
import com.sparta.delivery.menu.dto.request.MenuRequest;
import com.sparta.delivery.menu.dto.response.MenuResponse;
import com.sparta.delivery.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuResponse create(@AuthenticationPrincipal AuthUser authUser,
                               @Valid @RequestBody MenuRequest request) {
        return menuService.create(authUser, request);
    }

    @GetMapping
    public List<MenuResponse> getMenus() {
        return menuService.getMenus();
    }

    @GetMapping("/{menuId}")
    public MenuResponse getMenu(@PathVariable Long menuId) {
        return menuService.getMenu(menuId);
    }

    @PutMapping("/{menuId}")
    public MenuResponse update(@AuthenticationPrincipal AuthUser authUser,
                               @PathVariable Long menuId,
                               @Valid @RequestBody MenuRequest request) {
        return menuService.update(authUser, menuId, request);
    }

    @DeleteMapping("/{menuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long menuId) {
        menuService.delete(authUser, menuId);
    }
}
