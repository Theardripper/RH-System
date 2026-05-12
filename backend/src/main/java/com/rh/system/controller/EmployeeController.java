package com.rh.system.controller;


import com.rh.system.dto.request.EmployeeRequest;
import com.rh.system.dto.response.EmployeeResponse;
import com.rh.system.dto.response.PageResponse;
import com.rh.system.service.EmployeeService;
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

/**
 * Controller REST para Funcionários.
 *
 * Endpoints:
 * GET    /api/employees                    → lista paginada com filtros
 * GET    /api/employees/{id}               → busca por id
 * POST   /api/employees                    → cria (ADMIN, MANAGER)
 * PUT    /api/employees/{id}               → atualiza (ADMIN, MANAGER)
 * PATCH  /api/employees/{id}/deactivate    → desativa (ADMIN)
 * PATCH  /api/employees/{id}/activate      → reativa (ADMIN)
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Gestão de funcionários")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Lista funcionários ativos com paginação, busca global e filtro por departamento")
    public ResponseEntity<PageResponse<EmployeeResponse>> findAll(
            @RequestParam(required = false)          String search,
            @RequestParam(required = false)          Long   departmentId,
            @RequestParam(defaultValue = "0")        int    page,
            @RequestParam(defaultValue = "10")       int    size,
            @RequestParam(defaultValue = "firstName") String sort,
            @RequestParam(defaultValue = "asc")      String direction
    ) {
        Sort.Direction dir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        PageRequest pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return ResponseEntity.ok(employeeService.findAll(search, departmentId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca funcionário por ID")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cria novo funcionário (ADMIN ou MANAGER)")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Atualiza dados do funcionário (ADMIN ou MANAGER)")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativa funcionário — soft delete (ADMIN)")
    public ResponseEntity<EmployeeResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reativa funcionário inativo (ADMIN)")
    public ResponseEntity<EmployeeResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.activate(id));
    }
}