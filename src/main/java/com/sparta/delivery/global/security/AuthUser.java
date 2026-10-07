package com.sparta.delivery.global.security;

import com.sparta.delivery.user.entity.Role;

// JWT 에서 꺼낸 "지금 요청한 사람". 컨트롤러에서 @AuthenticationPrincipal 로 받는다.
public record AuthUser(String username, Role role) {
}
