package com.rh.system.service;

import com.hrSystem.hr.dto.request.EmployeeRequest;
import com.hrSystem.hr.dto.response.EmployeeResponse;
import com.hrSystem.hr.dto.response.PageResponse;
import com.hrSystem.hr.entity.Department;
import com.hrSystem.hr.entity.Employee;
import com.hrSystem.hr.exception.BusinessException;
import com.hrSystem.hr.exception.ConflictException;
import com.hrSystem.hr.exception.ResourceNotFoundException;
import com.hrSystem.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de Funcionário.
 * Centraliza todas as regras de negócio relacionadas a Employee.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository  employeeRepository;
    private final DepartmentService   departmentService;

    // ----------------------------------------------------------------
    // Leitura
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> findAll(String search, Long departmentId, Pageable pageable) {
        Page<Employee> page;

        if (departmentId != null) {
            page = employeeRepository.findByDepartmentId(departmentId, pageable);
        } else if (search != null && !search.isBlank()) {
            page = employeeRepository.searchActiveEmployees(search, pageable);
        } else {
            page = employeeRepository.findAllActive(pageable);
        }

        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return toResponse(findEmployeeOrThrow(id));
    }

    // ----------------------------------------------------------------
    // Escrita
    // ----------------------------------------------------------------

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        log.info("Criando funcionário: {} {}", request.getFirstName(), request.getLastName());

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Já existe um funcionário com o e-mail: " + request.getEmail());
        }

        Employee employee = buildEmployee(request, new Employee());
        Employee saved = employeeRepository.save(employee);

        log.info("Funcionário criado com id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        log.info("Atualizando funcionário id: {}", id);

        Employee employee = findEmployeeOrThrow(id);

        // Verifica conflito de email somente se mudou
        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Já existe um funcionário com o e-mail: " + request.getEmail());
        }

        buildEmployee(request, employee);
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse deactivate(Long id) {
        log.info("Desativando funcionário id: {}", id);

        Employee employee = findEmployeeOrThrow(id);

        if (!employee.getActive()) {
            throw new BusinessException("Funcionário já está inativo");
        }

        employee.setActive(false);
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse activate(Long id) {
        log.info("Reativando funcionário id: {}", id);

        Employee employee = findEmployeeOrThrow(id);

        if (employee.getActive()) {
            throw new BusinessException("Funcionário já está ativo");
        }

        employee.setActive(true);
        return toResponse(employeeRepository.save(employee));
    }

    // ----------------------------------------------------------------
    // Helpers internos
    // ----------------------------------------------------------------

    public Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário", id));
    }

    /**
     * Preenche ou atualiza os campos de um Employee a partir do request.
     * Reutilizado em create e update para evitar duplicação.
     */
    private Employee buildEmployee(EmployeeRequest request, Employee employee) {
        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setEmail(request.getEmail().toLowerCase().trim());
        employee.setPhone(request.getPhone());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());
        employee.setPosition(request.getPosition());

        if (request.getDepartmentId() != null) {
            Department dept = departmentService.findDepartmentOrThrow(request.getDepartmentId());
            employee.setDepartment(dept);
        } else {
            employee.setDepartment(null);
        }

        return employee;
    }

    public EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .hireDate(e.getHireDate())
                .salary(e.getSalary())
                .position(e.getPosition())
                .active(e.getActive())
                .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}