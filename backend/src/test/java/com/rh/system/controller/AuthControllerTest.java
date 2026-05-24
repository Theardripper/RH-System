package com.rh.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rh.system.dto.request.LoginRequest;
import com.rh.system.dto.request.RegisterRequest;
import com.rh.system.dto.response.AuthResponse;
import com.rh.system.entity.UserRole;
import com.rh.system.exception.ConflictException;
import com.rh.system.repository.UserRepository;
import com.rh.system.service.JwtService;
import com.rh.system.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.definition.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiJ9.mocktoken")
                .tokenType("Bearer")
                .userId(1L).username("admin")
                .email("admin@hrsystem.com")
                .role(UserRole.ADMIN)
                .build();
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("deve retornar 200 com token para credenciais válidas")
        void shouldReturn200WithToken() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("admin", "Admin@1234"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.username").value("admin"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @DisplayName("deve retornar 401 para credenciais inválidas")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            when(authService.login(any()))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("admin", "errada"))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("deve retornar 400 para body inválido")
        void shouldReturn400ForInvalidBody() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("", ""))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 201 ao registrar com sucesso")
        void shouldReturn201WhenRegistered() throws Exception {
            when(authService.register(any())).thenReturn(authResponse);

            RegisterRequest request = new RegisterRequest(
                    "novouser", "novo@hrsystem.com", "Senha@123", UserRole.EMPLOYEE, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("deve retornar 403 para role insuficiente")
        void shouldReturn403ForInsufficientRole() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "novouser", "novo@hrsystem.com", "Senha@123", UserRole.EMPLOYEE, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 409 para username duplicado")
        void shouldReturn409ForDuplicateUser() throws Exception {
            when(authService.register(any()))
                    .thenThrow(new ConflictException("Username já está em uso"));

            RegisterRequest request = new RegisterRequest(
                    "admin", "admin@hrsystem.com", "Senha@123", UserRole.ADMIN, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }
}
