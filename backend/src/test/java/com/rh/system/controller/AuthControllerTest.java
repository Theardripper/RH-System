package com.rh.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrSystem.hr.TestFactory;
import com.hrSystem.hr.config.SecurityConfig;
import com.hrSystem.hr.dto.request.LoginRequest;
import com.hrSystem.hr.dto.response.AuthResponse;
import com.hrSystem.hr.entity.UserRole;
import com.hrSystem.hr.exception.ConflictException;
import com.hrSystem.hr.security.JwtAuthenticationFilter;
import com.hrSystem.hr.security.JwtService;
import com.hrSystem.hr.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private com.hrSystem.hr.repository.UserRepository userRepository;

    private AuthResponse authResponse;

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
    }

    // ================================================================
    // POST /api/auth/login
    // ================================================================
    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("deve retornar 200 com token para credenciais válidas")
        void shouldReturn200WithTokenForValidCredentials() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TestFactory.buildLoginRequest())))
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
                            .content(objectMapper.writeValueAsString(TestFactory.buildLoginRequest())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
        }

        @Test
        @DisplayName("deve retornar 400 quando body está inválido")
        void shouldReturn400ForInvalidBody() throws Exception {
            LoginRequest invalid = new LoginRequest("", "");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields").exists());
        }
    }

    // ================================================================
    // POST /api/auth/register
    // ================================================================
    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 201 ao registrar com sucesso")
        void shouldReturn201WhenRegistered() throws Exception {
            when(authService.register(any())).thenReturn(authResponse);

            var request = new com.hrSystem.hr.dto.request.RegisterRequest(
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
            var request = new com.hrSystem.hr.dto.request.RegisterRequest(
                    "novouser", "novo@hrsystem.com", "Senha@123", UserRole.EMPLOYEE, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 409 para username ou email duplicado")
        void shouldReturn409ForDuplicateUser() throws Exception {
            when(authService.register(any()))
                    .thenThrow(new ConflictException("Username já está em uso"));

            var request = new com.hrSystem.hr.dto.request.RegisterRequest(
                    "admin", "admin@hrsystem.com", "Senha@123", UserRole.ADMIN, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }
}
