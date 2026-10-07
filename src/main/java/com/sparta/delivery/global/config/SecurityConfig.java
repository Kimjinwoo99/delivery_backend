package com.sparta.delivery.global.config;

import com.sparta.delivery.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // JWT 로 인증하는 API 라서 CSRF 보호와 세션을 쓰지 않는다.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 예외가 나면 /error 로 한 번 더 전달된다. 막아두면 400·404·409 가 전부 빈 403 이 된다.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menus/**").permitAll()
                        // 메뉴: 사장님만
                        .requestMatchers(HttpMethod.POST, "/api/menus").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/menus/*").hasRole("OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/menus/*").hasRole("OWNER")
                        // 주문·결제: 생성, 취소, 결제는 손님 / 상태 변경은 사장님 / 목록은 로그인한 사용자(anyRequest)
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/cancel").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/payments").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
