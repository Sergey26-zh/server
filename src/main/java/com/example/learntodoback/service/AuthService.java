package com.example.learntodoback.service;

import com.example.learntodoback.dto.auth.AuthRequestDto;
import com.example.learntodoback.dto.auth.AuthResponseDto;
import com.example.learntodoback.dto.auth.RegisterRequestDto;
import com.example.learntodoback.dto.auth.RegisterResponseDto;
import com.example.learntodoback.entity.User;
import com.example.learntodoback.exception.AuthException;
import com.example.learntodoback.repository.UserRepository;
import com.example.learntodoback.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public RegisterResponseDto register(RegisterRequestDto request, HttpServletResponse response) {
        if (userRepository.existsByUsername(request.getUsername())
                || userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Имя пользователя или адрес электронной почты уже занято!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername());
        RegisterResponseDto registerResponseDto = RegisterResponseDto.builder()
                .id(user.getId())
                .username(request.getUsername())
                .email(request.getEmail())
                .token(token)
                .build();
        response.setHeader("Authorization", "Bearer " + token);
        log.info("JWT Token: {}", token);
        return registerResponseDto;
    }

    public AuthResponseDto login(AuthRequestDto request, HttpServletResponse response) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException("Неверное имя пользователя или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Неверное имя пользователя или пароль");
        }
        String token = jwtUtil.generateToken(user.getUsername());
        AuthResponseDto authResponseDto = AuthResponseDto.builder()
                .id(user.getId())
                .username(request.getUsername())
                .token(token)
                .build();
        response.setHeader("Authorization", "Bearer " + token);

        log.info("JWT Token: {}", token);
        return authResponseDto;
    }
}
