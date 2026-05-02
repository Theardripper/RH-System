package com.rh.system.repository;

import com.hrSystem.hr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA para Employee.
 *
 * Estende JpaSpecificationExecutor para permitir filtros dinâmicos
 * via Specification (evita múltiplos métodos findBy).
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>,
                                             JpaSpecificationExecutor<Employee> {

    boolean existsByEmail(String email);

    Optional<Employee> findByEmail(String email);

    /**
     * Busca funcionários ativos com paginação.
     * JOIN FETCH carrega o departamento junto para evitar N+1 queries.
     *
     * Nota: countQuery separada é necessária quando usamos JOIN FETCH com paginação.
     */
    @Query(
        value = """
                SELECT e FROM Employee e
                LEFT JOIN FETCH e.department d
                WHERE e.active = true
                """,
        countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.active = true"
    )
    Page<Employee> findAllActive(Pageable pageable);

    /**
     * Busca por nome (primeiro ou último) ou email — filtro de pesquisa global.
     */
    @Query(
        value = """
                SELECT e FROM Employee e
                LEFT JOIN FETCH e.department
                WHERE e.active = true
                AND (
                    LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(e.email)     LIKE LOWER(CONCAT('%', :search, '%'))
                )
                """,
        countQuery = """
                SELECT COUNT(e) FROM Employee e
                WHERE e.active = true
                AND (
                    LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(e.email)     LIKE LOWER(CONCAT('%', :search, '%'))
                )
                """
    )
    Page<Employee> searchActiveEmployees(@Param("search") String search, Pageable pageable);

    /**
     * Busca funcionários por departamento com paginação.
     */
    @Query(
        value = "SELECT e FROM Employee e WHERE e.department.id = :deptId AND e.active = true",
        countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.department.id = :deptId AND e.active = true"
    )
    Page<Employee> findByDepartmentId(@Param("deptId") Long departmentId, Pageable pageable);
}