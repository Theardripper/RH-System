package com.rh.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entidade de usuário do sistema.
 *
 * Implementa UserDetails do Spring Security para integração direta
 * com o mecanismo de autenticação sem precisar de um wrapper.
 *
 * Mapeamento Hibernate:
 * - @OneToOne com Employee: FK employee_id na tabela users
 * - @Enumerated(STRING) para role (legível no banco)
 * - password armazena hash BCrypt
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email",    columnList = "email"),
                @Index(name = "idx_users_employee", columnList = "employee_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "employee"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Papel do usuário mapeado como String para ser legível no banco.
     * Ex: "ADMIN", "MANAGER", "EMPLOYEE"
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.EMPLOYEE;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Relacionamento OneToOne com Employee.
     * @JoinColumn define a FK employee_id na tabela users.
     * optional = true pois pode existir admin sem funcionário vinculado.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_user_employee")
    )
    private Employee employee;

    // ----------------------------------------------------------------
    // Implementação de UserDetails (Spring Security)
    // ----------------------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(active);
    }
}

