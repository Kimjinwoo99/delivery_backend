package com.sparta.delivery.user.service;

import com.sparta.delivery.global.security.JwtUtil;
import com.sparta.delivery.user.dto.request.LoginRequest;
import com.sparta.delivery.user.dto.request.SignupRequest;
import com.sparta.delivery.user.dto.response.LoginResponse;
import com.sparta.delivery.user.dto.response.UserResponse;
import com.sparta.delivery.user.entity.User;
import com.sparta.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DUPLICATE_USERNAME = "이미 사용 중인 아이디입니다.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_USERNAME);
        }

        User user = new User(request.username(), passwordEncoder.encode(request.password()), request.role());
        try {
            // 거의 동시에 들어온 가입 요청은 위 검사를 둘 다 통과할 수 있다. 이때는 DB 의 unique 제약이 막는다.
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_USERNAME);
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 아이디가 없는 경우와 비밀번호가 틀린 경우를 구분하지 않는다(계정 존재 여부 노출 방지).
        User user = userRepository.findByUsername(request.username())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        return LoginResponse.of(jwtUtil.createToken(user.getUsername(), user.getRole()));
    }
}
