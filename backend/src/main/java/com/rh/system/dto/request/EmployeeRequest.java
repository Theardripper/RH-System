package com.rh.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para criação e atualização de Funcionário.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String firstName;

    @NotBlank(message = "O sobrenome é obrigatório")
    @Size(min = 2, max = 100, message = "O sobrenome deve ter entre 2 e 100 caracteres")
    private String lastName;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Size(max = 150)
    private String email;

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    private String phone;

    @NotNull(message = "A data de contratação é obrigatória")
    @PastOrPresent(message = "A data de contratação não pode ser futura")
    private LocalDate hireDate;

    @NotNull(message = "O salário é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O salário deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Salário inválido")
    private BigDecimal salary;

    @Size(max = 100, message = "O cargo deve ter no máximo 100 caracteres")
    private String position;

    private Long departmentId;
}