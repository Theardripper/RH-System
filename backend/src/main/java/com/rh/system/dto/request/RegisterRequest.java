package com.rh.system.dto.request;


import com.rh.system.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para registro de novo usuário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "O username é obrigatório")
    @Size(min = 3, max = 80, message = "Username deve ter entre 3 e 80 caracteres")
    private String username;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "A senha deve conter letra maiúscula, minúscula, número e caractere especial"
    )
    private String password;

    private UserRole role = UserRole.EMPLOYEE;

    private Long employeeId;
}