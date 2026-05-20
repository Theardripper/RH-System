package com.rh.system;

import com.rh.system.dto.request.DepartmentRequest;
import com.rh.system.dto.request.EmployeeRequest;
import com.rh.system.dto.request.LoginRequest;
import com.rh.system.dto.request.VacationRequestDto;
import com.rh.system.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestFactory {

    public static Department buildDepartment() {
        return Department.builder().id(1L).name("Tecnologia").description("Departamento de TI").build();
    }

    public static Department buildDepartment(Long id, String name) {
        return Department.builder().id(id).name(name).description("Descrição de " + name).build();
    }

    public static Employee buildEmployee() {
        return Employee.builder().id(1L).firstName("João").lastName("Silva")
                .email("joao.silva@hrsystem.com").phone("(81) 99999-1111")
                .hireDate(LocalDate.of(2022, 1, 15)).salary(new BigDecimal("5000.00"))
                .position("Desenvolvedor").active(true).department(buildDepartment()).build();
    }

    public static Employee buildEmployee(Long id, String email) {
        return Employee.builder().id(id).firstName("Funcionário").lastName("Teste")
                .email(email).hireDate(LocalDate.now().minusYears(1))
                .salary(new BigDecimal("4000.00")).active(true).build();
    }

    public static User buildUser() {
        return User.builder().id(1L).username("admin").email("admin@hrsystem.com")
                .password("$2a$12$encodedPassword").role(UserRole.ADMIN)
                .active(true).employee(buildEmployee()).build();
    }

    public static User buildUser(UserRole role) {
        return User.builder().id(2L).username(role.name().toLowerCase())
                .email(role.name().toLowerCase() + "@hrsystem.com")
                .password("$2a$12$encodedPassword").role(role).active(true).build();
    }

    public static VacationRequest buildVacationRequest() {
        return VacationRequest.builder().id(1L).employee(buildEmployee())
                .startDate(LocalDate.now().plusDays(10)).endDate(LocalDate.now().plusDays(20))
                .status(VacationStatus.PENDING).reason("Férias anuais").build();
    }

    public static VacationRequest buildVacationRequest(VacationStatus status) {
        VacationRequest req = buildVacationRequest();
        req.setStatus(status);
        return req;
    }

    public static DepartmentRequest buildDepartmentRequest() {
        return DepartmentRequest.builder().name("Novo Departamento").description("Descrição").build();
    }

    public static EmployeeRequest buildEmployeeRequest() {
        return EmployeeRequest.builder().firstName("Maria").lastName("Costa")
                .email("maria.costa@hrsystem.com").phone("(81) 99888-2222")
                .hireDate(LocalDate.now().minusMonths(3)).salary(new BigDecimal("6000.00"))
                .position("Analista").departmentId(1L).build();
    }

    public static VacationRequestDto buildVacationRequestDto() {
        return VacationRequestDto.builder().startDate(LocalDate.now().plusDays(15))
                .endDate(LocalDate.now().plusDays(25)).reason("Férias programadas").build();
    }

    public static LoginRequest buildLoginRequest() {
        return new LoginRequest("admin", "Admin@1234");
    }
}
