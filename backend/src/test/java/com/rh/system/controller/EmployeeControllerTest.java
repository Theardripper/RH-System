package com.rh.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrSystem.hr.TestFactory;
import com.hrSystem.hr.config.SecurityConfig;
import com.hrSystem.hr.dto.request.EmployeeRequest;
import com.hrSystem.hr.dto.response.EmployeeResponse;
import com.hrSystem.hr.dto.response.PageResponse;
import com.hrSystem.hr.exception.ConflictException;
import com.hrSystem.hr.exception.ResourceNotFoundException;
import com.hrSystem.hr.security.JwtAuthenticationFilter;
import com.hrSystem.hr.security.JwtService;
import com.hrSystem.hr.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("EmployeeController")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private com.hrSystem.hr.repository.UserRepository userRepository;

    private EmployeeResponse employeeResponse;
    private EmployeeRequest  employeeRequest;

    @BeforeEach
    void setUp() {
        employeeResponse = EmployeeResponse.builder()
                .id(1L)
                .firstName("João")
                .lastName("Silva")
                .fullName("João Silva")
                .email("joao.silva@hrsystem.com")
                .hireDate(LocalDate.of(2022, 1, 15))
                .salary(new BigDecimal("5000.00"))
                .position("Desenvolvedor")
                .active(true)
                .departmentId(1L)
                .departmentName("Tecnologia")
                .build();

        employeeRequest = TestFactory.buildEmployeeRequest();
    }

    // ================================================================
    // GET /api/employees
    // ================================================================
    @Nested
    @DisplayName("GET /api/employees")
    class FindAll {

        @Test
        @WithMockUser
        @DisplayName("deve retornar 200 com lista paginada")
        void shouldReturn200WithPage() throws Exception {
            PageResponse<EmployeeResponse> page = PageResponse.<EmployeeResponse>builder()
                    .content(List.of(employeeResponse))
                    .page(0).size(10).totalElements(1).totalPages(1)
                    .first(true).last(true)
                    .build();

            when(employeeService.findAll(any(), any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].fullName").value("João Silva"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("deve aceitar parâmetros de paginação e filtro")
        void shouldAcceptPaginationParams() throws Exception {
            PageResponse<EmployeeResponse> page = PageResponse.<EmployeeResponse>builder()
                    .content(List.of()).page(0).size(5)
                    .totalElements(0).totalPages(0)
                    .first(true).last(true).build();

            when(employeeService.findAll(any(), any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/employees")
                            .param("search", "joão")
                            .param("page", "0")
                            .param("size", "5")
                            .param("sort", "lastName")
                            .param("direction", "desc"))
                    .andExpect(status().isOk());

            verify(employeeService).findAll(eq("joão"), isNull(), any());
        }
    }

    // ================================================================
    // GET /api/employees/{id}
    // ================================================================
    @Nested
    @DisplayName("GET /api/employees/{id}")
    class FindById {

        @Test
        @WithMockUser
        @DisplayName("deve retornar 200 com dados do funcionário")
        void shouldReturn200WhenFound() throws Exception {
            when(employeeService.findById(1L)).thenReturn(employeeResponse);

            mockMvc.perform(get("/api/employees/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("joao.silva@hrsystem.com"))
                    .andExpect(jsonPath("$.departmentName").value("Tecnologia"));
        }

        @Test
        @WithMockUser
        @DisplayName("deve retornar 404 quando não encontrado")
        void shouldReturn404WhenNotFound() throws Exception {
            when(employeeService.findById(99L))
                    .thenThrow(new ResourceNotFoundException("Funcionário", 99L));

            mockMvc.perform(get("/api/employees/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // POST /api/employees
    // ================================================================
    @Nested
    @DisplayName("POST /api/employees")
    class Create {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 201 ao criar com sucesso")
        void shouldReturn201WhenCreated() throws Exception {
            when(employeeService.create(any(EmployeeRequest.class))).thenReturn(employeeResponse);

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.fullName").value("João Silva"));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("deve retornar 201 para role MANAGER")
        void shouldReturn201ForManager() throws Exception {
            when(employeeService.create(any())).thenReturn(employeeResponse);

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("deve retornar 403 para role EMPLOYEE")
        void shouldReturn403ForEmployee() throws Exception {
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 400 para body inválido")
        void shouldReturn400ForInvalidBody() throws Exception {
            EmployeeRequest invalid = new EmployeeRequest();

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 409 para email duplicado")
        void shouldReturn409ForDuplicateEmail() throws Exception {
            when(employeeService.create(any()))
                    .thenThrow(new ConflictException("E-mail já cadastrado"));

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isConflict());
        }
    }

    // ================================================================
    // PATCH /api/employees/{id}/deactivate
    // ================================================================
    @Nested
    @DisplayName("PATCH /api/employees/{id}/deactivate e activate")
    class Activation {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 200 ao desativar funcionário")
        void shouldReturn200WhenDeactivated() throws Exception {
            EmployeeResponse inactive = EmployeeResponse.builder()
                    .id(1L).fullName("João Silva").active(false).build();
            when(employeeService.deactivate(1L)).thenReturn(inactive);

            mockMvc.perform(patch("/api/employees/1/deactivate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("deve retornar 200 ao reativar funcionário")
        void shouldReturn200WhenActivated() throws Exception {
            when(employeeService.activate(1L)).thenReturn(employeeResponse);

            mockMvc.perform(patch("/api/employees/1/activate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(true));
        }
    }
}
