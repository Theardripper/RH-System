package com.rh.system.controller;

import com.rh.system.dto.request.VacationRequestDto;
import com.rh.system.dto.response.PageResponse;
import com.rh.system.dto.response.VacationResponse;
import com.rh.system.entity.User;
import com.rh.system.entity.VacationStatus;
import com.rh.system.service.VacationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para Solicitações de Férias.
 *
 * Endpoints:
 * GET    /api/vacations                        → lista por status (ADMIN/MANAGER)
 * GET    /api/vacations/employee/{employeeId}  → lista por funcionário
 * GET    /api/vacations/{id}                   → busca por id
 * GET    /api/vacations/pending/count          → total pendentes (dashboard)
 * POST   /api/vacations/employee/{employeeId}  → cria solicitação
 * PATCH  /api/vacations/{id}/approve           → aprova (ADMIN/MANAGER)
 * PATCH  /api/vacations/{id}/reject            → rejeita (ADMIN/MANAGER)
 * PATCH  /api/vacations/{id}/cancel            → cancela (próprio funcionário)
 */
@RestController
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
@Tag(name = "Vacations", description = "Gestão de solicitações de férias")
@SecurityRequirement(name = "bearerAuth")
public class VacationController {

    private final VacationService vacationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lista solicitações por status com paginação (ADMIN ou MANAGER)")
    public ResponseEntity<PageResponse<VacationResponse>> findAll(
            @RequestParam(required = false) VacationStatus status,
            @RequestParam(defaultValue = "0")       int page,
            @RequestParam(defaultValue = "10")      int size,
            @RequestParam(defaultValue = "createdAt") String sort
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sort).descending());

        PageResponse<VacationResponse> response = (status != null)
                ? vacationService.findByStatus(status, pageable)
                : vacationService.findByStatus(VacationStatus.PENDING, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Lista solicitações de um funcionário específico")
    public ResponseEntity<PageResponse<VacationResponse>> findByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(vacationService.findByEmployee(employeeId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca solicitação por ID")
    public ResponseEntity<VacationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vacationService.findById(id));
    }

    @GetMapping("/pending/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Retorna total de solicitações pendentes — usado no dashboard")
    public ResponseEntity<Long> countPending() {
        return ResponseEntity.ok(vacationService.countPending());
    }

    @PostMapping("/employee/{employeeId}")
    @Operation(summary = "Cria solicitação de férias para um funcionário")
    public ResponseEntity<VacationResponse> create(
            @PathVariable Long employeeId,
            @Valid @RequestBody VacationRequestDto dto
    ) {
        VacationResponse response = vacationService.create(employeeId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Aprova solicitação pendente (ADMIN ou MANAGER)")
    public ResponseEntity<VacationResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(vacationService.approve(id, currentUser));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Rejeita solicitação pendente (ADMIN ou MANAGER)")
    public ResponseEntity<VacationResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(vacationService.reject(id, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancela solicitação pendente — somente o próprio funcionário")
    public ResponseEntity<VacationResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        Long employeeId = currentUser.getEmployee() != null
                ? currentUser.getEmployee().getId()
                : null;
        return ResponseEntity.ok(vacationService.cancel(id, employeeId));
    }
}