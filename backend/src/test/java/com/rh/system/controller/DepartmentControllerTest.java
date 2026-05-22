package com.rh.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rh.system.config.SecurityConfig;
import com.rh.system.dto.request.DepartmentRequest;
import com.rh.system.dto.response.DepartmentResponse;
import com.rh.system.dto.response.PageResponse;
import com.rh.system.exception.ConflictException;
import com.rh.system.exception.ResourceNotFoundException;
import com.rh.system.repository.UserRepository;
import com.rh.system.security.JwtAuthenticationFilter;
import com.rh.system.service.DepartmentService;
import com.rh.system.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.definition.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Boot 4.x:
 * - @MockitoBean substitui @MockBean (pacote org.springframework.test.context.bean.definition)
 * - @WebMvcTest continua funcionando normalmente
 */
@WebMvcTest(DepartmentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("DepartmentController")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ Spring Boot 4: @MockitoBean no lugar de @MockBean
    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private DepartmentResponse departmentResponse;
    private DepartmentRequest departmentRequest;

    @BeforeEach
    void setUp() {
        departmentResponse = DepartmentResponse.builder()
                .id(1L).name("Tecnologia").description("Departamento de TI")
                .activeEmployeesCount(3).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        departmentRequest = DepartmentRequest.builder()
                .name("Novo Departamento").description("Descrição")
                .build();
    }

    @Nested
    @DisplayName("GET /api/departments")
    class FindAll {

        @Test
        @WithMockUser
        @DisplayName("deve retornar 200 com lista paginada")
        void shouldReturn200WithPagedList() throws Exception {
            PageResponse<DepartmentResponse> page = PageResponse.<DepartmentResponse>builder()
                    .content(List.of(departmentResponse))
                    .page(0).size(10).totalElements(1).totalPages(1)
                    .first(true).last(true).build();

            when(departmentService.findAll(any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/departments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Tecnologia"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("deve retornar 403 quando não autenticado")
        void shouldReturn403WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/departments"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/departments/{id}")
    class FindById {

        @Test
        @WithMockUser
        @DisplayName("deve retornar 200 quando departamento existe")
        void shouldReturn200WhenFound() throws Exception {
            when(departmentService.findById(1L)).thenReturn(departmentResponse);

            mockMvc.perform(get("/api/departments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Tecnologia"));
        }

        @Test
        @WithMockUser
        @DisplayName("deve retornar 404 quando não existe")
        void shouldReturn404WhenNotFound() throws Exception {
            when(departmentService.findById(99L))
                    .thenThrow(new ResourceNotFoundException("Departamento", 99L));

            mockMvc.perform(get("/api/departments/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/departments")
    class Create {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 201 ao criar com sucesso")
        void shouldReturn201WhenCreated() throws Exception {
            when(departmentService.create(any(DepartmentRequest.class))).thenReturn(departmentResponse);

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(departmentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Tecnologia"));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("deve retornar 403 para role insuficiente")
        void shouldReturn403ForInsufficientRole() throws Exception {
            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(departmentRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 400 para body inválido")
        void shouldReturn400ForInvalidBody() throws Exception {
            DepartmentRequest invalid = new DepartmentRequest("", null);

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 409 quando nome já existe")
        void shouldReturn409WhenConflict() throws Exception {
            when(departmentService.create(any())).thenThrow(new ConflictException("Nome já existe"));

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(departmentRequest)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("DELETE /api/departments/{id}")
    class Delete {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 204 ao deletar com sucesso")
        void shouldReturn204WhenDeleted() throws Exception {
            doNothing().when(departmentService).delete(1L);

            mockMvc.perform(delete("/api/departments/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 409 quando há funcionários ativos")
        void shouldReturn409WhenHasActiveEmployees() throws Exception {
            doThrow(new ConflictException("Possui funcionários ativos"))
                    .when(departmentService).delete(1L);

            mockMvc.perform(delete("/api/departments/1"))
                    .andExpect(status().isConflict());
        }
    }
}
