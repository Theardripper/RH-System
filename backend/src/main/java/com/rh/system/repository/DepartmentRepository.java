package com.rh.system.repository;

import com.hrSystem.hr.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA para Department.
 *
 * Spring Data gera automaticamente as queries básicas (findAll, findById, save, delete).
 * Queries customizadas usam JPQL (@Query) para manter portabilidade com o Hibernate.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Department> findByNameIgnoreCase(String name);

    /**
     * Busca departamentos pelo nome com paginação.
     * JPQL — Hibernate traduz para SQL nativo do PostgreSQL.
     */
    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Department> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Conta quantos funcionários ativos existem em um departamento.
     * Usa JOIN FETCH implícito via JPQL para evitar N+1.
     */
    @Query("""
            SELECT COUNT(e) FROM Employee e
            WHERE e.department.id = :departmentId
            AND e.active = true
            """)
    long countActiveEmployeesByDepartment(@Param("departmentId") Long departmentId);
}