package com.rh.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um departamento da empresa.
 *
 * Utiliza soft delete via @SQLDelete para manter histórico,
 * e @SQLRestriction para filtrar automaticamente registros deletados.
 *
 * Mapeamento Hibernate:
 * - @OneToMany com LAZY loading para evitar N+1 queries
 * - cascade = CascadeType.ALL para persistência automática de filhos
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "employees")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    /**
     * Relação bidirecional com Employee.
     * mappedBy aponta para o campo "department" em Employee.
     * orphanRemoval = true remove employees sem departamento ao deletar.
     */
    @OneToMany(
            mappedBy = "department",
            cascade = CascadeType.ALL,
            orphanRemoval = false,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    // ----------------------------------------------------------------
    // Métodos auxiliares para manter consistência bidirecional
    // ----------------------------------------------------------------

    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setDepartment(this);
    }

    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.setDepartment(null);
    }
}
