import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { EmployeeService } from '@core/services/employee.service';
import { DepartmentService } from '@core/services/department.service';
import { VacationService } from '@core/services/vacation.service';
import { forkJoin } from 'rxjs';

interface StatCard {
  label: string;
  value: number | string;
  icon: string;
  color: string;
  route: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="dashboard">

      <div class="page-header">
        <div>
          <h1>Dashboard</h1>
          <p class="text-muted">
            Bem-vindo, <strong>{{ currentUser()?.username }}</strong>!
            Aqui está um resumo do sistema.
          </p>
        </div>
      </div>

      <!-- Cards de estatísticas -->
      <div class="stats-grid">
        @for (stat of stats(); track stat.label) {
          <a [routerLink]="stat.route" class="stat-card" [style.--accent]="stat.color">
            <div class="stat-icon">{{ stat.icon }}</div>
            <div class="stat-info">
              <span class="stat-value">
                @if (loading()) { — } @else { {{ stat.value }} }
              </span>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
            <div class="stat-arrow">→</div>
          </a>
        }
      </div>

      <!-- Ações rápidas -->
      <div class="quick-actions card">
        <h2>Ações Rápidas</h2>
        <div class="actions-grid">
          <a routerLink="/employees" class="action-btn">
            <span>👥</span>
            <span>Ver Funcionários</span>
          </a>
          @if (isAdminOrManager()) {
            <a routerLink="/vacations" class="action-btn">
              <span>🏖️</span>
              <span>Gerenciar Férias</span>
            </a>
          }
          @if (isAdmin()) {
            <a routerLink="/departments" class="action-btn">
              <span>🏢</span>
              <span>Departamentos</span>
            </a>
            <a routerLink="/auth/register" class="action-btn">
              <span>➕</span>
              <span>Novo Usuário</span>
            </a>
          }
        </div>
      </div>

    </div>
  `,
  styles: [`
    .dashboard { max-width: 1100px; }

    .page-header {
      margin-bottom: 1.5rem;

      h1 {
        font-size: 1.5rem;
        font-weight: 700;
        color: var(--color-text);
        margin-bottom: 0.25rem;
      }
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .stat-card {
      background: white;
      border: 1px solid var(--color-border);
      border-radius: var(--radius-lg);
      padding: 1.25rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      text-decoration: none;
      transition: all 0.2s ease;
      border-left: 4px solid var(--accent);

      &:hover {
        box-shadow: var(--shadow-md);
        transform: translateY(-2px);
      }

      .stat-icon {
        font-size: 2rem;
        width: 48px;
        text-align: center;
      }

      .stat-info {
        flex: 1;
        display: flex;
        flex-direction: column;

        .stat-value {
          font-size: 1.75rem;
          font-weight: 700;
          color: var(--color-text);
          line-height: 1;
        }

        .stat-label {
          font-size: 0.8rem;
          color: var(--color-text-muted);
          margin-top: 0.25rem;
        }
      }

      .stat-arrow {
        color: var(--color-text-muted);
        font-size: 1.1rem;
      }
    }

    .quick-actions {
      h2 {
        font-size: 1rem;
        font-weight: 600;
        margin-bottom: 1rem;
      }
    }

    .actions-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 0.75rem;
    }

    .action-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.625rem 1rem;
      background: var(--color-bg);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-md);
      font-size: 0.875rem;
      color: var(--color-text);
      text-decoration: none;
      transition: all 0.15s ease;

      &:hover {
        background: var(--color-primary-light);
        border-color: var(--color-primary);
        color: var(--color-primary);
      }
    }
  `]
})
export class DashboardComponent implements OnInit {

  loading = signal(true);
  stats   = signal<StatCard[]>([
    { label: 'Funcionários Ativos', value: 0, icon: '👥', color: '#2563eb', route: '/employees' },
    { label: 'Departamentos',       value: 0, icon: '🏢', color: '#7c3aed', route: '/departments' },
    { label: 'Férias Pendentes',    value: 0, icon: '🏖️', color: '#d97706', route: '/vacations' },
  ]);

  readonly currentUser      = this.authService.currentUser;
  readonly isAdmin          = this.authService.isAdmin;
  readonly isAdminOrManager = this.authService.isAdminOrManager;

  constructor(
    private authService: AuthService,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private vacationService: VacationService
  ) {}

  ngOnInit(): void {
    forkJoin({
      employees:   this.employeeService.findAll({ size: 1 }),
      departments: this.departmentService.findAll({ size: 1 }),
      pending:     this.vacationService.countPending()
    }).subscribe({
      next: ({ employees, departments, pending }) => {
        this.stats.set([
          { label: 'Funcionários Ativos', value: employees.totalElements,   icon: '👥', color: '#2563eb', route: '/employees' },
          { label: 'Departamentos',       value: departments.totalElements, icon: '🏢', color: '#7c3aed', route: '/departments' },
          { label: 'Férias Pendentes',    value: pending,                   icon: '🏖️', color: '#d97706', route: '/vacations' },
        ]);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
