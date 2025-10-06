package com.taskService.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public OncePerRequestFilter mockJwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws IOException, ServletException {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken("1", null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OncePerRequestFilter mockJwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(mockJwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

