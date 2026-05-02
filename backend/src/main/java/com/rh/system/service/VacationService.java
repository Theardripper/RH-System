package com.rh.system.service;

import com.hrSystem.hr.dto.request.VacationRequestDto;
import com.hrSystem.hr.dto.response.PageResponse;
import com.hrSystem.hr.dto.response.VacationResponse;
import com.hrSystem.hr.entity.Employee;
import com.hrSystem.hr.entity.User;
import com.hrSystem.hr.entity.VacationRequest;
import com.hrSystem.hr.entity.VacationStatus;
import com.hrSystem.hr.exception.BusinessException;
import com.hrSystem.hr.exception.ResourceNotFoundException;
import com.hrSystem.hr.repository.VacationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de Solicitações de Férias.
 * Contém regras de negócio como validação de conflito de datas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VacationService {

    private final VacationRequestRepository vacationRepository;
    private final EmployeeService           employeeService;

    // ----------------------------------------------------------------
    // Leitura
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<VacationResponse> findByEmployee(Long employeeId, Pageable pageable) {
        Page<VacationRequest> page = vacationRepository.findByEmployeeId(employeeId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<VacationResponse> findByStatus(VacationStatus status, Pageable pageable) {
        Page<VacationRequest> page = vacationRepository.findByStatus(status, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public VacationResponse findById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return vacationRepository.countByStatus(VacationStatus.PENDING);
    }

    // ----------------------------------------------------------------
    // Escrita
    // ----------------------------------------------------------------

    @Transactional
    public VacationResponse create(Long employeeId, VacationRequestDto dto) {
        log.info("Criando solicitação de férias para funcionário id: {}", employeeId);

        // Regra 1: data de fim deve ser >= data de início
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("A data de fim não pode ser anterior à data de início");
        }

        Employee employee = employeeService.findEmployeeOrThrow(employeeId);

        if (!employee.getActive()) {
            throw new BusinessException("Não é possível solicitar férias para um funcionário inativo");
        }

        // Regra 2: verifica conflito de datas com outras solicitações
        List<VacationRequest> conflicts = vacationRepository.findConflictingRequests(
                employeeId, dto.getStartDate(), dto.getEndDate()
        );

        if (!conflicts.isEmpty()) {
            throw new BusinessException(
                "Já existe uma solicitação de férias no período de "
                + dto.getStartDate() + " a " + dto.getEndDate()
            );
        }

        VacationRequest request = VacationRequest.builder()
                .employee(employee)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status(VacationStatus.PENDING)
                .build();

        VacationRequest saved = vacationRepository.save(request);
        log.info("Solicitação de férias criada com id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public VacationResponse approve(Long id, User reviewer) {
        log.info("Aprovando solicitação de férias id: {} por: {}", id, reviewer.getUsername());

        VacationRequest request = findOrThrow(id);

        if (!request.isPending()) {
            throw new BusinessException("Apenas solicitações PENDENTES podem ser aprovadas");
        }

        request.approve(reviewer);
        return toResponse(vacationRepository.save(request));
    }

    @Transactional
    public VacationResponse reject(Long id, User reviewer) {
        log.info("Rejeitando solicitação de férias id: {} por: {}", id, reviewer.getUsername());

        VacationRequest request = findOrThrow(id);

        if (!request.isPending()) {
            throw new BusinessException("Apenas solicitações PENDENTES podem ser rejeitadas");
        }

        request.reject(reviewer);
        return toResponse(vacationRepository.save(request));
    }

    @Transactional
    public VacationResponse cancel(Long id, Long requestingEmployeeId) {
        log.info("Cancelando solicitação de férias id: {}", id);

        VacationRequest request = findOrThrow(id);

        if (!request.getEmployee().getId().equals(requestingEmployeeId)) {
            throw new BusinessException("Você só pode cancelar suas próprias solicitações");
        }

        if (!request.isPending()) {
            throw new BusinessException("Apenas solicitações PENDENTES podem ser canceladas");
        }

        request.setStatus(VacationStatus.CANCELLED);
        return toResponse(vacationRepository.save(request));
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private VacationRequest findOrThrow(Long id) {
        return vacationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação de férias", id));
    }

    private VacationResponse toResponse(VacationRequest v) {
        return VacationResponse.builder()
                .id(v.getId())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .totalDays(v.getTotalDays())
                .status(v.getStatus())
                .reason(v.getReason())
                .employeeId(v.getEmployee().getId())
                .employeeFullName(v.getEmployee().getFullName())
                .reviewedById(v.getReviewer() != null ? v.getReviewer().getId() : null)
                .reviewedByUsername(v.getReviewer() != null ? v.getReviewer().getUsername() : null)
                .reviewedAt(v.getReviewedAt())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}