package com.rh.system.repository;

import com.rh.system.entity.VacationRequest;
import com.rh.system.entity.VacationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {

    /**
     * Lista solicitações de um funcionário com paginação.
     * JOIN FETCH carrega employee e department juntos.
     */
    @Query(
        value = """
                SELECT v FROM VacationRequest v
                JOIN FETCH v.employee e
                LEFT JOIN FETCH e.department
                WHERE e.id = :employeeId
                """,
        countQuery = "SELECT COUNT(v) FROM VacationRequest v WHERE v.employee.id = :employeeId"
    )
    Page<VacationRequest> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    /**
     * Lista todas as solicitações por status com paginação.
     */
    @Query(
        value = """
                SELECT v FROM VacationRequest v
                JOIN FETCH v.employee e
                LEFT JOIN FETCH e.department
                WHERE v.status = :status
                """,
        countQuery = "SELECT COUNT(v) FROM VacationRequest v WHERE v.status = :status"
    )
    Page<VacationRequest> findByStatus(@Param("status") VacationStatus status, Pageable pageable);

    /**
     * Verifica conflito de datas para um funcionário.
     * Impede aprovação de férias sobrepostas.
     */
    @Query("""
            SELECT v FROM VacationRequest v
            WHERE v.employee.id = :employeeId
            AND v.status IN ('PENDING', 'APPROVED')
            AND v.startDate <= :endDate
            AND v.endDate   >= :startDate
            """)
    List<VacationRequest> findConflictingRequests(
            @Param("employeeId") Long employeeId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate
    );

    /**
     * Conta solicitações pendentes — usado no dashboard.
     */
    long countByStatus(VacationStatus status);
}