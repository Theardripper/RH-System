package com.rh.system.controller;

import com.rh.system.dto.request.DepartmentRequest;
import com.rh.system.dto.response.DepartmentResponse;
import com.rh.system.dto.response.PageResponse;
import com.rh.system.exception.ConflictException;
import com.rh.system.exception.ResourceNotFoundException;
import com.rh.system.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes do DepartmentController — Spring Boot 4 / Spring 7 compatível.
 *
 * Estratégia: teste direto ao controller (sem MockMvc) usando Mockito puro.
 * @WebMvcTest e @AutoConfigureMockMvc foram removidos no Boot 4.
 * @MockitoBean não existe no spring-test 7.x.
 *
 * Testamos a lógica do controller diretamente: chamadas ao service,
 * status codes e conteúdo das respostas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentController")
class DepartmentControllerTest {

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private DepartmentResponse departmentResponse;
    private DepartmentRequest  departmentRequest;

    @BeforeEach
    void setUp() {
        departmentResponse = DepartmentResponse.builder()
                .id(1L)
                .name("Tecnologia")
                .description("Departamento de TI")
                .activeEmployeesCount(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        departmentRequest = DepartmentRequest.builder()
                .name("Novo Departamento")
                .description("Descrição")
                .build();
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar departamento quando id existe")
        void shouldReturnDepartmentWhenFound() {
            when(departmentService.findById(1L)).thenReturn(departmentResponse);

            var response = departmentController.findById(1L);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("Tecnologia");
            assertThat(response.getBody().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve propagar ResourceNotFoundException quando não encontrado")
        void shouldPropagateExceptionWhenNotFound() {
            when(departmentService.findById(99L))
                    .thenThrow(new ResourceNotFoundException("Departamento", 99L));

            assertThatThrownBy(() -> departmentController.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve retornar 201 ao criar com sucesso")
        void shouldReturn201WhenCreated() {
            when(departmentService.create(any(DepartmentRequest.class)))
                    .thenReturn(departmentResponse);

            var response = departmentController.create(departmentRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(201);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("Tecnologia");
            verify(departmentService).create(departmentRequest);
        }

        @Test
        @DisplayName("deve propagar ConflictException quando nome duplicado")
        void shouldPropagateConflictException() {
            when(departmentService.create(any()))
                    .thenThrow(new ConflictException("Nome já existe"));

            assertThatThrownBy(() -> departmentController.create(departmentRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Nome já existe");
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("deve retornar 200 ao atualizar com sucesso")
        void shouldReturn200WhenUpdated() {
            when(departmentService.update(eq(1L), any(DepartmentRequest.class)))
                    .thenReturn(departmentResponse);

            var response = departmentController.update(1L, departmentRequest);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            verify(departmentService).update(1L, departmentRequest);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("deve retornar 204 ao deletar com sucesso")
        void shouldReturn204WhenDeleted() {
            doNothing().when(departmentService).delete(1L);

            var response = departmentController.delete(1L);

            assertThat(response.getStatusCode().value()).isEqualTo(204);
            verify(departmentService).delete(1L);
        }

        @Test
        @DisplayName("deve propagar ConflictException quando há funcionários ativos")
        void shouldPropagateConflictWhenHasActiveEmployees() {
            doThrow(new ConflictException("Possui funcionários ativos"))
                    .when(departmentService).delete(1L);

            assertThatThrownBy(() -> departmentController.delete(1L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("funcionários ativos");
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("deve retornar página de departamentos")
        void shouldReturnPage() {
            PageResponse<DepartmentResponse> page = PageResponse.<DepartmentResponse>builder()
                    .content(List.of(departmentResponse))
                    .page(0).size(10).totalElements(1).totalPages(1)
                    .first(true).last(true).build();

            when(departmentService.findAll(any(), any())).thenReturn(page);

            var response = departmentController.findAll(null, 0, 10, "name");

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        }
    }
}