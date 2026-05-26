// ============================================================
// Models do Sistema de RH
// Espelham os DTOs de Response do backend
// ============================================================

export type UserRole = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';
export type VacationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

// ----------------------------------------------------------------
// Auth
// ----------------------------------------------------------------
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: UserRole;
  employeeId?: number;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  role: UserRole;
  employeeId?: number;
}

// ----------------------------------------------------------------
// Department
// ----------------------------------------------------------------
export interface Department {
  id: number;
  name: string;
  description?: string;
  activeEmployeesCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface DepartmentRequest {
  name: string;
  description?: string;
}

// ----------------------------------------------------------------
// Employee
// ----------------------------------------------------------------
export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone?: string;
  hireDate: string;
  salary: number;
  position?: string;
  active: boolean;
  departmentId?: number;
  departmentName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface EmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  hireDate: string;
  salary: number;
  position?: string;
  departmentId?: number;
}

// ----------------------------------------------------------------
// Vacation
// ----------------------------------------------------------------
export interface VacationRequest {
  id: number;
  startDate: string;
  endDate: string;
  totalDays: number;
  status: VacationStatus;
  reason?: string;
  employeeId: number;
  employeeFullName: string;
  reviewedById?: number;
  reviewedByUsername?: string;
  reviewedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface VacationRequestForm {
  startDate: string;
  endDate: string;
  reason?: string;
}

// ----------------------------------------------------------------
// Pagination
// ----------------------------------------------------------------
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
  search?: string;
  departmentId?: number;
  status?: VacationStatus;
}
