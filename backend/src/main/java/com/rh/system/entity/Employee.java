package com.rh.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um funcionário.
 *
 * Mapeamento Hibernate:
 * - @ManyToOne com LAZY loading para o departamento
 * - @OneToMany com LAZY loading para solicitações de férias
 * - @OneToOne com User (mapeado pelo lado do User)
 * - @Column com precision/scale para salário (evita float/double)
 */
@Entity
@Table(
        name = "employees",
        indexes = {
                @Index(name = "idx_employees_email",      columnList = "email"),
                @Index(name = "idx_employees_active",     columnList = "active"),
                @Index(name = "idx_employees_department", columnList = "department_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"department", "vacationRequests", "user"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    /**
     * Salário com precisão de 12 dígitos e 2 casas decimais.
     * BigDecimal é obrigatório para valores monetários.
     */
    @Column(name = "salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * ManyToOne com LAZY loading.
     * @JoinColumn define a FK no banco (department_id).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_employee_department"))
    private Department department;

    /**
     * OneToMany com LAZY loading para férias.
     * cascade PERSIST/MERGE para salvar solicitações junto ao funcionário.
     */
    @OneToMany(
            mappedBy = "employee",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<VacationRequest> vacationRequests = new ArrayList<>();

    /**
     * Relacionamento inverso com User (mapeado pelo User).
     */
    @OneToOne(mappedBy = "employee", fetch = FetchType.LAZY)
    private User user;

    // ----------------------------------------------------------------
    // Método utilitário
    // ----------------------------------------------------------------

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addVacationRequest(VacationRequest request) {
        vacationRequests.add(request);
        request.setEmployee(this);
    }
}