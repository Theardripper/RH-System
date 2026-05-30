import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VacationService } from '@core/services/vacation.service';
import { AuthService } from '@core/services/auth.service';
import { VacationRequest, VacationStatus, PageResponse } from '@core/models';

@Component({
  selector: 'app-vacation-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page">

      <div class="page-header">
        <div>
          <h1>Solicitações de Férias</h1>
          <p class="text-muted">{{ page()?.totalElements ?? 0 }} solicitações</p>
        </div>
      </div>

      <!-- Filtro de status -->
      <div class="card filters-card">
        <div class="status-tabs">
          @for (tab of statusTabs; track tab.value) {
            <button
              class="tab-btn"
              [class.active]="selectedStatus === tab.value"
              (click)="onStatusChange(tab.value)"
            >
              {{ tab.label }}
            </button>
          }
        </div>
      </div>

      <!-- Tabela -->
      <div class="card table-card">
        @if (loading()) {
          <div class="loading-state">
            <div class="spinner-lg"></div>
          </div>
        } @else if (page()?.content?.length === 0) {
          <div class="empty-state">
            <span>🏖️</span>
            <p>Nenhuma solicitação encontrada</p>
          </div>
        } @else {
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Funcionário</th>
                  <th>Período</th>
                  <th>Dias</th>
                  <th>Motivo</th>
                  <th>Status</th>
                  <th>Solicitado em</th>
                  @if (canManage()) { <th>Ações</th> }
                </tr>
              </thead>
              <tbody>
                @for (v of page()?.content; track v.id) {
                  <tr>
                    <td>
                      <div class="employee-cell">
                        <div class="avatar-sm">
                          {{ v.employeeFullName[0] }}
                        </div>
                        <span class="font-medium text-sm">{{ v.employeeFullName }}</span>
                      </div>
                    </td>
                    <td class="text-sm">
                      {{ v.startDate | date:'dd/MM/yyyy' }} →
                      {{ v.endDate   | date:'dd/MM/yyyy' }}
                    </td>
                    <td class="text-sm text-muted">{{ v.totalDays }}d</td>
                    <td class="text-sm text-muted">{{ v.reason || '—' }}</td>
                    <td>
                      <span class="badge" [class]="statusBadge(v.status)">
                        {{ statusLabel(v.status) }}
                      </span>
                    </td>
                    <td class="text-sm text-muted">
                      {{ v.createdAt | date:'dd/MM/yyyy' }}
                    </td>
                    @if (canManage()) {
                      <td>
                        @if (v.status === 'PENDING') {
                          <div class="actions">
                            <button
                              class="btn btn-success btn-sm"
                              (click)="approve(v)"
                              [disabled]="processing() === v.id"
                            >
                              ✓ Aprovar
                            </button>
                            <button
                              class="btn btn-danger btn-sm"
                              (click)="reject(v)"
                              [disabled]="processing() === v.id"
                            >
                              ✗ Rejeitar
                            </button>
                          </div>
                        } @else {
                          <span class="text-muted text-sm">—</span>
                        }
                      </td>
                    }
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <div class="pagination">
            <span class="pagination-info">
              {{ page()?.totalElements }} solicitações no total
            </span>
            <div class="pagination-controls">
              <button
                class="btn btn-secondary btn-sm"
                [disabled]="page()?.first"
                (click)="goToPage(currentPage - 1)"
              >
                ← Anterior
              </button>
              <span class="page-indicator">
                {{ currentPage + 1 }} / {{ page()?.totalPages }}
              </span>
              <button
                class="btn btn-secondary btn-sm"
                [disabled]="page()?.last"
                (click)="goToPage(currentPage + 1)"
              >
                Próxima →
              </button>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      margin-bottom: 1.25rem;
      h1 { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.25rem; }
    }

    .filters-card { margin-bottom: 1rem; padding: 0.75rem 1rem; }

    .status-tabs {
      display: flex;
      gap: 0.25rem;
      flex-wrap: wrap;
    }

    .tab-btn {
      padding: 0.375rem 0.875rem;
      border: 1px solid var(--color-border);
      border-radius: var(--radius-md);
      background: white;
      font-size: 0.8rem;
      cursor: pointer;
      color: var(--color-text-muted);
      transition: all 0.15s;

      &:hover { border-color: var(--color-primary); color: var(--color-primary); }
      &.active { background: var(--color-primary); border-color: var(--color-primary); color: white; }
    }

    .table-card { padding: 0; overflow: hidden; }

    .loading-state, .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem;
      gap: 1rem;
      color: var(--color-text-muted);
      span { font-size: 3rem; }
    }

    .spinner-lg {
      width: 40px;
      height: 40px;
      border: 3px solid var(--color-border);
      border-top-color: var(--color-primary);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    .employee-cell {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .avatar-sm {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: var(--color-primary-light);
      color: var(--color-primary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.7rem;
      font-weight: 700;
      flex-shrink: 0;
    }

    .actions { display: flex; gap: 0.375rem; }

    .page-indicator {
      font-size: 0.875rem;
      color: var(--color-text-muted);
      padding: 0 0.5rem;
    }

    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class VacationListComponent implements OnInit {

  page       = signal<PageResponse<VacationRequest> | null>(null);
  loading    = signal(true);
  processing = signal<number | null>(null);

  selectedStatus: VacationStatus | undefined = 'PENDING';
  currentPage = 0;

  readonly canManage = this.authService.isAdminOrManager;

  readonly statusTabs = [
    { label: 'Pendentes',   value: 'PENDING'   as VacationStatus },
    { label: 'Aprovadas',   value: 'APPROVED'  as VacationStatus },
    { label: 'Rejeitadas',  value: 'REJECTED'  as VacationStatus },
    { label: 'Canceladas',  value: 'CANCELLED' as VacationStatus },
  ];

  constructor(
    private vacationService: VacationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.vacationService.findAll({
      page:   this.currentPage,
      size:   10,
      status: this.selectedStatus
    }).subscribe({
      next: page => { this.page.set(page); this.loading.set(false); },
      error: ()  => this.loading.set(false)
    });
  }

  onStatusChange(status: VacationStatus): void {
    this.selectedStatus = status;
    this.currentPage    = 0;
    this.load();
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.load();
  }

  approve(v: VacationRequest): void {
    this.processing.set(v.id);
    this.vacationService.approve(v.id).subscribe({
      next: updated => {
        this.updateInList(updated);
        this.processing.set(null);
      },
      error: () => this.processing.set(null)
    });
  }

  reject(v: VacationRequest): void {
    this.processing.set(v.id);
    this.vacationService.reject(v.id).subscribe({
      next: updated => {
        this.updateInList(updated);
        this.processing.set(null);
      },
      error: () => this.processing.set(null)
    });
  }

  private updateInList(updated: VacationRequest): void {
    const current = this.page();
    if (current) {
      this.page.set({
        ...current,
        content: current.content.map(v => v.id === updated.id ? updated : v)
      });
    }
  }

  statusBadge(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'badge-warning', APPROVED: 'badge-success',
      REJECTED: 'badge-danger', CANCELLED: 'badge-muted'
    };
    return map[status] ?? 'badge-muted';
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'Pendente', APPROVED: 'Aprovada',
      REJECTED: 'Rejeitada', CANCELLED: 'Cancelada'
    };
    return map[status] ?? status;
  }
}
