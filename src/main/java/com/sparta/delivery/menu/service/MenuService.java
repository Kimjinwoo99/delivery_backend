package com.sparta.delivery.menu.service;

import com.sparta.delivery.global.security.AuthUser;
import com.sparta.delivery.menu.dto.request.MenuRequest;
import com.sparta.delivery.menu.dto.response.MenuResponse;
import com.sparta.delivery.menu.entity.Menu;
import com.sparta.delivery.menu.repository.MenuRepository;
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
public class MenuService {

    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    @Transactional
    public MenuResponse create(AuthUser authUser, MenuRequest request) {
        // 메뉴 주인은 요청 본문이 아니라 토큰의 사용자다.
        User owner = userRepository.findByUsername(authUser.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."));

        Menu menu = menuRepository.save(new Menu(owner, request.name(), request.price(), request.description()));
        return MenuResponse.from(menu);
    }

    public List<MenuResponse> getMenus() {
        return menuRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                .map(MenuResponse::from)
                .toList();
    }

    public MenuResponse getMenu(Long menuId) {
        return MenuResponse.from(findActiveMenu(menuId));
    }

    @Transactional
    public MenuResponse update(AuthUser authUser, Long menuId, MenuRequest request) {
        Menu menu = findOwnedMenu(authUser, menuId);
        menu.update(request.name(), request.price(), request.description());
        // 수정 시각(JPA Auditing)은 flush 때 채워지므로, 응답에 새 값이 담기도록 먼저 반영한다.
        menuRepository.flush();
        return MenuResponse.from(menu);
    }

    @Transactional
    public void delete(AuthUser authUser, Long menuId) {
        findOwnedMenu(authUser, menuId).delete();
    }

    // 없거나 삭제된 메뉴는 404
    private Menu findActiveMenu(Long menuId) {
        return menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."));
    }

    // 404(대상 없음) 다음 403(남의 메뉴) 순서로 확인한다.
    private Menu findOwnedMenu(AuthUser authUser, Long menuId) {
        Menu menu = findActiveMenu(menuId);
        if (!menu.isOwnedBy(authUser.username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 메뉴만 수정·삭제할 수 있습니다.");
        }
        return menu;
    }
}
