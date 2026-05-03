package com.rh.system.entity;

/**
 * Enum de papéis de usuário no sistema.
 * Mapeado como String no banco via @Enumerated(EnumType.STRING).
 */
public enum UserRole {
    ADMIN,
    MANAGER,
    EMPLOYEE
}