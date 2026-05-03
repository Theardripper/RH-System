package com.rh.system.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de entrada para solicitação de férias.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationRequestDto {

    @NotNull(message = "A data de início é obrigatória")
    @Future(message = "A data de início deve ser futura")
    private LocalDate startDate;

    @NotNull(message = "A data de fim é obrigatória")
    @Future(message = "A data de fim deve ser futura")
    private LocalDate endDate;

    @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
    private String reason;
}