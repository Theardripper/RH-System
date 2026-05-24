package com.rh.system.controller;

import com.rh.system.dto.request.LoginRequest;
import com.rh.system.dto.request.RegisterRequest;
import com.rh.system.dto.response.AuthResponse;
import com.rh.system.entity.UserRole;
import com.rh.system.exception.ConflictException;
import com.rh.system.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthResponse authResponse;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiJ9.mocktoken")
                .tokenType("Bearer")
                .userId(1L)
                .username("admin")
                .email("admin@hrsystem.com")
                .role(UserRole.ADMIN)
                .build();

        loginRequest = new LoginRequest("admin", "Admin@1234");

        registerRequest = new RegisterRequest(
                "novouser", "novo@hrsystem.com", "Senha@123", UserRole.EMPLOYEE, null
        );
    }

    // ================================================================
    // login()
    // ================================================================
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("deve retornar 200 com token para credenciais válidas")
        void shouldReturn200WithTokenForValidCredentials() {
            when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

            var response = authController.login(loginRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getToken()).isNotBlank();
            assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
            assertThat(response.getBody().getUsername()).isEqualTo("admin");
            assertThat(response.getBody().getRole()).isEqualTo(UserRole.ADMIN);
            verify(authService).login(loginRequest);
        }

        @Test
        @DisplayName("deve propagar BadCredentialsException para credenciais inválidas")
        void shouldPropagateBadCredentialsException() {
            when(authService.login(any()))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            assertThatThrownBy(() -> authController.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Credenciais inválidas");

            verify(authService).login(loginRequest);
        }

        @Test
        @DisplayName("deve chamar authService.login com os dados corretos")
        void shouldCallServiceWithCorrectData() {
            when(authService.login(any())).thenReturn(authResponse);

            authController.login(loginRequest);

            verify(authService).login(argThat(req ->
                    req.getUsername().equals("admin") &&
                            req.getPassword().equals("Admin@1234")
            ));
        }
    }

    // ================================================================
    // register()
    // ================================================================
    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("deve retornar 201 ao registrar com sucesso")
        void shouldReturn201WhenRegistered() {
            when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

            var response = authController.register(registerRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(201);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getToken()).isNotBlank();
            assertThat(response.getBody().getUsername()).isEqualTo("admin");
            verify(authService).register(registerRequest);
        }

        @Test
        @DisplayName("deve propagar ConflictException para username duplicado")
        void shouldPropagateConflictForDuplicateUsername() {
            when(authService.register(any()))
                    .thenThrow(new ConflictException("Username já está em uso: novouser"));

            assertThatThrownBy(() -> authController.register(registerRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("novouser");

            verify(authService).register(registerRequest);
        }

        @Test
        @DisplayName("deve propagar ConflictException para email duplicado")
        void shouldPropagateConflictForDuplicateEmail() {
            when(authService.register(any()))
                    .thenThrow(new ConflictException("E-mail já está em uso: novo@hrsystem.com"));

            assertThatThrownBy(() -> authController.register(registerRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("novo@hrsystem.com");
        }

        @Test
        @DisplayName("deve chamar authService.register com os dados corretos")
        void shouldCallServiceWithCorrectData() {
            when(authService.register(any())).thenReturn(authResponse);

            authController.register(registerRequest);

            verify(authService).register(argThat(req ->
                    req.getUsername().equals("novouser") &&
                            req.getEmail().equals("novo@hrsystem.com") &&
                            req.getRole() == UserRole.EMPLOYEE
            ));
        }
    }
}