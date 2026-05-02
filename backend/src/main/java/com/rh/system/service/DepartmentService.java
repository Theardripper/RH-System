package com.rh.system.service;

import com.hrSystem.hr.dto.request.DepartmentRequest;
import com.hrSystem.hr.dto.response.DepartmentResponse;
import com.hrSystem.hr.dto.response.PageResponse;
import com.hrSystem.hr.entity.Department;
import com.hrSystem.hr.exception.ConflictException;
import com.hrSystem.hr.exception.ResourceNotFoundException;
import com.hrSystem.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de Departamento.
 *
 * @Transactional(readOnly = true) em métodos de leitura:
 * - Hibernate otimiza o flush mode (não faz dirty checking)
 * - Banco pode usar réplicas de leitura
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // ----------------------------------------------------------------
    // Leitura
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> findAll(String search, Pageable pageable) {
        Page<Department> page = (search != null && !search.isBlank())
                ? departmentRepository.findByNameContainingIgnoreCase(search, pageable)
                : departmentRepository.findAll(pageable);

        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        Department department = findDepartmentOrThrow(id);
        return toResponse(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAllList() {
        return departmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // Escrita
    // ----------------------------------------------------------------

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        log.info("Criando departamento: {}", request.getName());

        if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Já existe um departamento com o nome: " + request.getName());
        }

        Department department = Department.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        Department saved = departmentRepository.save(department);
        log.info("Departamento criado com id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        log.info("Atualizando departamento id: {}", id);

        Department department = findDepartmentOrThrow(id);

        // Verifica conflito de nome somente se o nome mudou
        if (!department.getName().equalsIgnoreCase(request.getName())
                && departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Já existe um departamento com o nome: " + request.getName());
        }

        department.setName(request.getName().trim());
        department.setDescription(request.getDescription());

        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deletando departamento id: {}", id);

        Department department = findDepartmentOrThrow(id);

        long activeEmployees = departmentRepository.countActiveEmployeesByDepartment(id);
        if (activeEmployees > 0) {
            throw new ConflictException(
                "Não é possível excluir o departamento pois possui " + activeEmployees + " funcionário(s) ativo(s)."
            );
        }

        departmentRepository.delete(department);
        log.info("Departamento id {} deletado", id);
    }

    // ----------------------------------------------------------------
    // Helpers internos
    // ----------------------------------------------------------------

    public Department findDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", id));
    }

    private DepartmentResponse toResponse(Department d) {
        long activeCount = departmentRepository.countActiveEmployeesByDepartment(d.getId());
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .activeEmployeesCount(activeCount)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}