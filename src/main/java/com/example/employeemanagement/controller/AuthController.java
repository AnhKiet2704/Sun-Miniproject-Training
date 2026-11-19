package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ApiResponse;
import com.example.employeemanagement.dto.AuthRequestDTO;
import com.example.employeemanagement.dto.AuthResponseDTO;
import com.example.employeemanagement.dto.RegisterRequestDTO;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.model.Role;
import com.example.employeemanagement.model.User;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Module 9: đăng ký + đăng nhập (JWT) cho REST API
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Tên đăng nhập đã được sử dụng: " + request.getUsername());
        }
        // Tự đăng ký luôn là ROLE_USER. Tài khoản ADMIN chỉ do hệ thống/quản trị tạo.
        userRepository.save(new User(null, request.getUsername(),
                passwordEncoder.encode(request.getPassword()), Role.ROLE_USER));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng ký thành công", request.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody AuthRequestDTO request) {
        // Sai mật khẩu -> BadCredentialsException -> GlobalExceptionHandler trả 401
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));
        String token = tokenProvider.generateToken(user.getUsername(), user.getRole().name());

        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công",
                new AuthResponseDTO(token, user.getUsername(), user.getRole().name())));
    }
}
