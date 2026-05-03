package com.rh.system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de saída para Departamento.
 * Inclui contagem de funcionários ativos calculada pelo service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private Long id;
    private String name;
    private String description;
    private long activeEmployeesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}