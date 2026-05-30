import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DepartmentService } from '@core/services/department.service';
import { Department, PageResponse } from '@core/models';

@Component({
  selector: 'app-department-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page">

      <div class="page-header">
        <div>
          <h1>Departamentos</h1>
          <p class="text-muted">{{ page()?.totalElements ?? 0 }} departamentos cadastrados</p>
        </div>
        <a routerLink="/departments/new" class="btn btn-primary">
          ➕ Novo Departamento
        </a>
      </div>

      <!-- Filtros -->
      <div class="card filters-card">
        <div class="filters">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input
              type="text"
              placeholder="Buscar departamento..."
              [(ngModel)]="searchTerm"
              (input)="onSearch()"
            />
          </div>
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
            <span>🏢</span>
            <p>Nenhum departamento encontrado</p>
            <a routerLink="/departments/new" class="btn btn-primary btn-sm">
              Criar departamento
            </a>
          </div>
        } @else {
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>Descrição</th>
                  <th>Funcionários Ativos</th>
                  <th>Criado em</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                @for (dept of page()?.content; track dept.id) {
                  <tr>
                    <td class="font-medium">{{ dept.name }}</td>
                    <td class="text-muted text-sm">{{ dept.description || '—' }}</td>
                    <td>
                      <span class="badge badge-info">
                        {{ dept.activeEmployeesCount }} funcionário(s)
                      </span>
                    </td>
                    <td class="text-sm text-muted">
                      {{ dept.createdAt | date:'dd/MM/yyyy' }}
                    </td>
                    <td>
                      <div class="actions">
                        <a
                          [routerLink]="['/departments', dept.id, 'edit']"
                          class="btn btn-secondary btn-sm"
                        >
                          ✏️ Editar
                        </a>
                        <button
                          class="btn btn-danger btn-sm"
                          (click)="confirmDelete(dept)"
                          [disabled]="dept.activeEmployeesCount > 0"
                          [title]="dept.activeEmployeesCount > 0 ? 'Remova os funcionários antes de excluir' : 'Excluir'"
                        >
                          🗑️ Excluir
                        </button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <div class="pagination">
            <span class="pagination-info">
              {{ page()?.totalElements }} departamentos no total
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

      <!-- Modal de confirmação de exclusão -->
      @if (deptToDelete()) {
        <div class="modal-overlay" (click)="deptToDelete.set(null)">
          <div class="modal" (click)="$event.stopPropagation()">
            <h3>Confirmar exclusão</h3>
            <p>
              Tem certeza que deseja excluir o departamento
              <strong>{{ deptToDelete()!.name }}</strong>?
              Essa ação não pode ser desfeita.
            </p>
            @if (deleteError()) {
              <div class="alert alert-error">{{ deleteError() }}</div>
            }
            <div class="modal-actions">
              <button class="btn btn-secondary" (click)="deptToDelete.set(null)">
                Cancelar
              </button>
              <button
                class="btn btn-danger"
                (click)="deleteDepartment()"
                [disabled]="deleting()"
              >
                @if (deleting()) { Excluindo... } @else { Excluir }
              </button>
            </div>
          </div>
        </div>
      }

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

    .filters-card { margin-bottom: 1rem; padding: 1rem; }

    .filters { display: flex; gap: 0.75rem; }

    .search-box {
      flex: 1;
      position: relative;
      .search-icon {
        position: absolute;
        left: 0.75rem;
        top: 50%;
        transform: translateY(-50%);
        font-size: 0.875rem;
      }
      input {
        width: 100%;
        padding: 0.5rem 0.75rem 0.5rem 2.25rem;
        border: 1px solid var(--color-border);
        border-radius: var(--radius-md);
        font-size: 0.875rem;
        outline: none;
        &:focus { border-color: var(--color-primary); }
      }
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

    .actions { display: flex; gap: 0.375rem; }

    .page-indicator {
      font-size: 0.875rem;
      color: var(--color-text-muted);
      padding: 0 0.5rem;
    }

    .modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 200;
    }

    .modal {
      background: white;
      border-radius: var(--radius-lg);
      padding: 1.5rem;
      max-width: 440px;
      width: 90%;
      box-shadow: var(--shadow-lg);

      h3 { font-size: 1.1rem; font-weight: 600; margin-bottom: 0.75rem; }
      p  { font-size: 0.9rem; color: var(--color-text-muted); margin-bottom: 1rem; }
    }

    .modal-actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
    }

    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class DepartmentListComponent implements OnInit {

  page         = signal<PageResponse<Department> | null>(null);
  loading      = signal(true);
  deleting     = signal(false);
  deleteError  = signal('');
  deptToDelete = signal<Department | null>(null);

  searchTerm  = '';
  currentPage = 0;

  constructor(private departmentService: DepartmentService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.departmentService.findAll({
      page:   this.currentPage,
      size:   10,
      search: this.searchTerm || undefined
    }).subscribe({
      next: page => { this.page.set(page); this.loading.set(false); },
      error: ()  => this.loading.set(false)
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.load();
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.load();
  }

  confirmDelete(dept: Department): void {
    this.deleteError.set('');
    this.deptToDelete.set(dept);
  }

  deleteDepartment(): void {
    const dept = this.deptToDelete();
    if (!dept) return;

    this.deleting.set(true);
    this.departmentService.delete(dept.id).subscribe({
      next: () => {
        this.deptToDelete.set(null);
        this.deleting.set(false);
        this.load();
      },
      error: (err) => {
        this.deleting.set(false);
        this.deleteError.set(err.error?.message ?? 'Erro ao excluir departamento.');
      }
    });
  }
}
