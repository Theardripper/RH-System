package com.rh.system.controller;


import com.rh.system.dto.request.DepartmentRequest;
import com.rh.system.dto.response.DepartmentResponse;
import com.rh.system.dto.response.PageResponse;
import com.rh.system.service.DepartmentService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para Departamentos.
 *
 * Endpoints:
 * GET    /api/departments          → lista paginada (todos os roles)
 * GET    /api/departments/all      → lista completa sem paginação (para selects)
 * GET    /api/departments/{id}     → busca por id
 * POST   /api/departments          → cria (ADMIN)
 * PUT    /api/departments/{id}     → atualiza (ADMIN)
 * DELETE /api/departments/{id}     → deleta (ADMIN)
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Gestão de departamentos")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Lista departamentos com paginação e filtro por nome")
    public ResponseEntity<PageResponse<DepartmentResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return ResponseEntity.ok(departmentService.findAll(search, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lista todos os departamentos sem paginação (para selects/dropdowns)")
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return ResponseEntity.ok(departmentService.findAllList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca departamento por ID")
    public ResponseEntity<DepartmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria novo departamento (ADMIN)")
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza departamento (ADMIN)")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deleta departamento (ADMIN) — só permite se não houver funcionários ativos")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}