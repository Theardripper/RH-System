package com.rh.system.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entidade de solicitação de férias.
 *
 * Mapeamento Hibernate:
 * - @ManyToOne com LAZY loading para Employee e User (reviewer)
 * - @Enumerated(STRING) para status legível no banco
 * - @Transient para calcular dias sem persistir
 */
@Entity
@Table(
        name = "vacation_requests",
        indexes = {
                @Index(name = "idx_vacation_employee", columnList = "employee_id"),
                @Index(name = "idx_vacation_status",   columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"employee", "reviewer"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)

public class VacationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Funcionário que fez a solicitação.
     * Obrigatório — não pode ser null.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_vacation_employee")
    )
    private Employee employee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VacationStatus status = VacationStatus.PENDING;

    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * Usuário que aprovou ou rejeitou (manager/admin).
     * Opcional — null enquanto pendente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reviewed_by",
            foreignKey = @ForeignKey(name = "fk_vacation_reviewer")
    )
    private User reviewer;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // ----------------------------------------------------------------
    // Campos calculados (não persistidos)
    // ----------------------------------------------------------------

    /**
     * Retorna a quantidade de dias da solicitação.
     * Não persistido no banco — calculado em memória.
     */
    @Transient
    public long getTotalDays() {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    // ----------------------------------------------------------------
    // Métodos de negócio
    // ----------------------------------------------------------------

    public void approve(User reviewer) {
        this.status = VacationStatus.APPROVED;
        this.reviewer = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(User reviewer) {
        this.status = VacationStatus.REJECTED;
        this.reviewer = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return VacationStatus.PENDING.equals(this.status);
    }
}
