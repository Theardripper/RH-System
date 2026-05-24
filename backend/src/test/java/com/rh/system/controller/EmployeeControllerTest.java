package com.rh.system.controller;

import com.rh.system.dto.request.EmployeeRequest;
import com.rh.system.dto.response.EmployeeResponse;
import com.rh.system.dto.response.PageResponse;
import com.rh.system.exception.BusinessException;
import com.rh.system.exception.ConflictException;
import com.rh.system.exception.ResourceNotFoundException;
import com.rh.system.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeController")
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private EmployeeResponse employeeResponse;
    private EmployeeRequest  employeeRequest;

    @BeforeEach
    void setUp() {
        employeeResponse = EmployeeResponse.builder()
                .id(1L).firstName("João").lastName("Silva").fullName("João Silva")
                .email("joao.silva@hrsystem.com")
                .hireDate(LocalDate.of(2022, 1, 15))
                .salary(new BigDecimal("5000.00"))
                .position("Desenvolvedor").active(true)
                .departmentId(1L).departmentName("Tecnologia")
                .build();

        employeeRequest = EmployeeRequest.builder()
                .firstName("Maria").lastName("Costa")
                .email("maria.costa@hrsystem.com")
                .phone("(81) 99888-2222")
                .hireDate(LocalDate.now().minusMonths(3))
                .salary(new BigDecimal("6000.00"))
                .position("Analista").departmentId(1L)
                .build();
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("deve retornar página de funcionários")
        void shouldReturnPage() {
            PageResponse<EmployeeResponse> page = PageResponse.<EmployeeResponse>builder()
                    .content(List.of(employeeResponse))
                    .page(0).size(10).totalElements(1).totalPages(1)
                    .first(true).last(true).build();

            when(employeeService.findAll(any(), any(), any())).thenReturn(page);

            var response = employeeController.findAll(null, null, 0, 10, "firstName", "asc");

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getFullName()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("deve passar search e departmentId ao service")
        void shouldPassFiltersToService() {
            PageResponse<EmployeeResponse> page = PageResponse.<EmployeeResponse>builder()
                    .content(List.of()).page(0).size(10)
                    .totalElements(0).totalPages(0)
                    .first(true).last(true).build();

            when(employeeService.findAll(eq("joão"), eq(1L), any())).thenReturn(page);

            employeeController.findAll("joão", 1L, 0, 10, "firstName", "asc");

            verify(employeeService).findAll(eq("joão"), eq(1L), any());
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar 200 com dados do funcionário")
        void shouldReturn200WhenFound() {
            when(employeeService.findById(1L)).thenReturn(employeeResponse);

            var response = employeeController.findById(1L);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getEmail()).isEqualTo("joao.silva@hrsystem.com");
            assertThat(response.getBody().getDepartmentName()).isEqualTo("Tecnologia");
        }

        @Test
        @DisplayName("deve propagar ResourceNotFoundException quando não encontrado")
        void shouldPropagateExceptionWhenNotFound() {
            when(employeeService.findById(99L))
                    .thenThrow(new ResourceNotFoundException("Funcionário", 99L));

            assertThatThrownBy(() -> employeeController.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve retornar 201 ao criar com sucesso")
        void shouldReturn201WhenCreated() {
            when(employeeService.create(any(EmployeeRequest.class))).thenReturn(employeeResponse);

            var response = employeeController.create(employeeRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(201);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFullName()).isEqualTo("João Silva");
            verify(employeeService).create(employeeRequest);
        }

        @Test
        @DisplayName("deve propagar ConflictException para email duplicado")
        void shouldPropagateConflictForDuplicateEmail() {
            when(employeeService.create(any()))
                    .thenThrow(new ConflictException("E-mail já cadastrado"));

            assertThatThrownBy(() -> employeeController.create(employeeRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("E-mail");
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("deve retornar 200 ao atualizar com sucesso")
        void shouldReturn200WhenUpdated() {
            when(employeeService.update(eq(1L), any(EmployeeRequest.class)))
                    .thenReturn(employeeResponse);

            var response = employeeController.update(1L, employeeRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            verify(employeeService).update(1L, employeeRequest);
        }
    }

    @Nested
    @DisplayName("deactivate() e activate()")
    class Activation {

        @Test
        @DisplayName("deve retornar 200 ao desativar funcionário")
        void shouldReturn200WhenDeactivated() {
            EmployeeResponse inactive = EmployeeResponse.builder()
                    .id(1L).fullName("João Silva").active(false).build();
            when(employeeService.deactivate(1L)).thenReturn(inactive);

            var response = employeeController.deactivate(1L);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getActive()).isFalse();
        }

        @Test
        @DisplayName("deve retornar 200 ao reativar funcionário")
        void shouldReturn200WhenActivated() {
            when(employeeService.activate(1L)).thenReturn(employeeResponse);

            var response = employeeController.activate(1L);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getActive()).isTrue();
        }

        @Test
        @DisplayName("deve propagar BusinessException ao desativar já inativo")
        void shouldPropagateBusinessExceptionWhenAlreadyInactive() {
            when(employeeService.deactivate(1L))
                    .thenThrow(new BusinessException("Funcionário já está inativo"));

            assertThatThrownBy(() -> employeeController.deactivate(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inativo");
        }
    }
}