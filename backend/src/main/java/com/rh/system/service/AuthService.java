package com.rh.system.service;

import com.hrSystem.hr.dto.request.LoginRequest;
import com.hrSystem.hr.dto.request.RegisterRequest;
import com.hrSystem.hr.dto.response.AuthResponse;
import com.hrSystem.hr.entity.Employee;
import com.hrSystem.hr.entity.User;
import com.hrSystem.hr.exception.ConflictException;
import com.hrSystem.hr.exception.ResourceNotFoundException;
import com.hrSystem.hr.repository.EmployeeRepository;
import com.hrSystem.hr.repository.UserRepository;
import com.hrSystem.hr.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de autenticação.
 * Delega a verificação de credenciais ao AuthenticationManager do Spring Security.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final EmployeeRepository    employeeRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;

    // ----------------------------------------------------------------
    // Login
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Tentativa de login: {}", request.getUsername());

        // Delega autenticação ao Spring Security (lança BadCredentialsException se falhar)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findActiveByUsernameWithEmployee(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        String token = jwtService.generateToken(user);
        log.info("Login bem-sucedido: {}", user.getUsername());

        return buildAuthResponse(user, token);
    }

    // ----------------------------------------------------------------
    // Registro
    // ----------------------------------------------------------------

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando usuário: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username já está em uso: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("E-mail já está em uso: " + request.getEmail());
        }

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário", request.getEmployeeId()));
        }

        User user = User.builder()
                .username(request.getUsername().trim().toLowerCase())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .employee(employee)
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        log.info("Usuário registrado com id: {}", saved.getId());
        return buildAuthResponse(saved, token);
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .employeeId(user.getEmployee() != null ? user.getEmployee().getId() : null)
                .build();
    }
}