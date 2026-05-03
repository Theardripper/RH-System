package com.rh.system.dto.response;


import com.rh.system.entity.VacationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de saída para Solicitação de Férias.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationResponse {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private VacationStatus status;
    private String reason;

    // Dados do funcionário
    private Long employeeId;
    private String employeeFullName;

    // Dados do revisor (null se ainda pendente)
    private Long reviewedById;
    private String reviewedByUsername;
    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}