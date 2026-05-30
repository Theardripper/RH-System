import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EmployeeService } from '@core/services/employee.service';
import { VacationService } from '@core/services/vacation.service';
import { AuthService } from '@core/services/auth.service';
import { Employee, VacationRequest, PageResponse } from '@core/models';

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">

      @if (loading()) {
        <div class="loading-state">
          <div class="spinner-lg"></div>
        </div>
      } @else if (employee()) {

        <!-- Header -->
        <div class="page-header">
          <div class="header-left">
            <a routerLink="/employees" class="back-link">← Funcionários</a>
            <div class="employee-title">
              <div class="avatar-lg">
                {{ employee()!.firstName[0] }}{{ employee()!.lastName[0] }}
              </div>
              <div>
                <h1>{{ employee()!.fullName }}</h1>
                <p class="text-muted">{{ employee()!.position || 'Sem cargo' }}</p>
              </div>
            </div>
          </div>
          @if (canManage()) {
            <div class="header-actions">
              <a [routerLink]="['/employees', employee()!.id, 'edit']" class="btn btn-secondary">
                ✏️ Editar
              </a>
              @if (employee()!.active) {
                <button class="btn btn-danger" (click)="toggleActive()">
                  Desativar
                </button>
              } @else {
                <button class="btn btn-success" (click)="toggleActive()">
                  Reativar
                </button>
              }
            </div>
          }
        </div>

        <!-- Info Cards -->
        <div class="info-grid">

          <div class="card info-card">
            <h3>Informações Pessoais</h3>
            <div class="info-list">
              <div class="info-row">
                <span class="info-label">E-mail</span>
                <span>{{ employee()!.email }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Telefone</span>
                <span>{{ employee()!.phone || '—' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Status</span>
                <span class="badge" [class]="employee()!.active ? 'badge-success' : 'badge-muted'">
                  {{ employee()!.active ? 'Ativo' : 'Inativo' }}
                </span>
              </div>
            </div>
          </div>

          <div class="card info-card">
            <h3>Dados Profissionais</h3>
            <div class="info-list">
              <div class="info-row">
                <span class="info-label">Departamento</span>
                <span>
                  @if (employee()!.departmentName) {
                    <span class="badge badge-info">{{ employee()!.departmentName }}</span>
                  } @else { — }
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">Data de Contratação</span>
                <span>{{ employee()!.hireDate | date:'dd/MM/yyyy' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Salário</span>
                <span>{{ employee()!.salary | currency:'BRL':'symbol':'1.2-2' }}</span>
              </div>
            </div>
          </div>

        </div>

        <!-- Solicitações de Férias -->
        <div class="card">
          <div class="section-header">
            <h3>Solicitações de Férias</h3>
          </div>

          @if (vacations()?.content?.length === 0) {
            <div class="empty-state-sm">
              <p>Nenhuma solicitação de férias</p>
            </div>
          } @else {
            <div class="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Período</th>
                    <th>Dias</th>
                    <th>Motivo</th>
                    <th>Status</th>
                    <th>Solicitado em</th>
                  </tr>
                </thead>
                <tbody>
                  @for (v of vacations()?.content; track v.id) {
                    <tr>
                      <td class="text-sm">
                        {{ v.startDate | date:'dd/MM/yyyy' }} →
                        {{ v.endDate   | date:'dd/MM/yyyy' }}
                      </td>
                      <td class="text-sm">{{ v.totalDays }} dias</td>
                      <td class="text-sm text-muted">{{ v.reason || '—' }}</td>
                      <td>
                        <span class="badge" [class]="statusBadge(v.status)">
                          {{ statusLabel(v.status) }}
                        </span>
                      </td>
                      <td class="text-sm text-muted">
                        {{ v.createdAt | date:'dd/MM/yyyy' }}
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>

      }
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .back-link {
      font-size: 0.875rem;
      color: var(--color-text-muted);
      text-decoration: none;
      display: inline-block;
      margin-bottom: 0.75rem;
      &:hover { color: var(--color-primary); }
    }

    .employee-title {
      display: flex;
      align-items: center;
      gap: 1rem;

      h1 { font-size: 1.5rem; font-weight: 700; }
    }

    .avatar-lg {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: var(--color-primary);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.1rem;
      font-weight: 700;
      flex-shrink: 0;
    }

    .header-actions {
      display: flex;
      gap: 0.75rem;
      align-items: center;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 1rem;
      margin-bottom: 1rem;
    }

    .info-card h3 {
      font-size: 0.875rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--color-text-muted);
      margin-bottom: 1rem;
    }

    .info-list { display: flex; flex-direction: column; gap: 0.75rem; }

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid var(--color-border);
      font-size: 0.875rem;

      &:last-child { border-bottom: none; padding-bottom: 0; }
    }

    .info-label { color: var(--color-text-muted); }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;

      h3 { font-size: 1rem; font-weight: 600; }
    }

    .empty-state-sm {
      text-align: center;
      padding: 2rem;
      color: var(--color-text-muted);
      font-size: 0.875rem;
    }

    .loading-state {
      display: flex;
      justify-content: center;
      padding: 3rem;
    }

    .spinner-lg {
      width: 40px;
      height: 40px;
      border: 3px solid var(--color-border);
      border-top-color: var(--color-primary);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class EmployeeDetailComponent implements OnInit {

  employee  = signal<Employee | null>(null);
  vacations = signal<PageResponse<VacationRequest> | null>(null);
  loading   = signal(true);

  readonly canManage = this.authService.isAdminOrManager;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeService,
    private vacationService: VacationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.router.navigate(['/employees']); return; }

    this.employeeService.findById(+id).subscribe({
      next: emp => {
        this.employee.set(emp);
        this.loading.set(false);
        this.loadVacations(+id);
      },
      error: () => this.router.navigate(['/employees'])
    });
  }

  loadVacations(employeeId: number): void {
    this.vacationService.findByEmployee(employeeId, { size: 10 }).subscribe({
      next: page => this.vacations.set(page)
    });
  }

  toggleActive(): void {
    const emp = this.employee();
    if (!emp) return;

    const action$ = emp.active
      ? this.employeeService.deactivate(emp.id)
      : this.employeeService.activate(emp.id);

    action$.subscribe({
      next: updated => this.employee.set(updated)
    });
  }

  statusBadge(status: string): string {
    const map: Record<string, string> = {
      PENDING:   'badge-warning',
      APPROVED:  'badge-success',
      REJECTED:  'badge-danger',
      CANCELLED: 'badge-muted'
    };
    return map[status] ?? 'badge-muted';
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING:   'Pendente',
      APPROVED:  'Aprovada',
      REJECTED:  'Rejeitada',
      CANCELLED: 'Cancelada'
    };
    return map[status] ?? status;
  }
}
