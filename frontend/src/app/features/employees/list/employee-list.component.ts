import { Component, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { EmployeeService } from '@core/services/employee.service';
import { DepartmentService } from '@core/services/department.service';
import { AuthService } from '@core/services/auth.service';
import { Employee, Department, PageResponse } from '@core/models';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page">

      <!-- Header -->
      <div class="page-header">
        <div>
          <h1>Funcionários</h1>
          <p class="text-muted">{{ page()?.totalElements ?? 0 }} funcionários cadastrados</p>
        </div>
        @if (canManage()) {
          <a routerLink="/employees/new" class="btn btn-primary">
            ➕ Novo Funcionário
          </a>
        }
      </div>

      <!-- Filtros -->
      <div class="card filters-card">
        <div class="filters">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input
              type="text"
              placeholder="Buscar por nome ou e-mail..."
              [(ngModel)]="searchTerm"
              (ngModelChange)="onSearch($event)"
            />
          </div>

          <select [(ngModel)]="selectedDepartment" (ngModelChange)="onDepartmentFilter($event)">
            <option [ngValue]="null">Todos os departamentos</option>
            @for (dept of departments(); track dept.id) {
              <option [ngValue]="dept.id">{{ dept.name }}</option>
            }
          </select>

          <select [(ngModel)]="pageSize" (ngModelChange)="onPageSizeChange($event)">
            <option [ngValue]="10">10 por página</option>
            <option [ngValue]="20">20 por página</option>
            <option [ngValue]="50">50 por página</option>
          </select>
        </div>
      </div>

      <!-- Tabela -->
      <div class="card table-card">
        @if (loading()) {
          <div class="loading-state">
            <div class="spinner-lg"></div>
            <p>Carregando funcionários...</p>
          </div>
        } @else if (page()?.content?.length === 0) {
          <div class="empty-state">
            <span>👥</span>
            <p>Nenhum funcionário encontrado</p>
            @if (canManage()) {
              <a routerLink="/employees/new" class="btn btn-primary btn-sm">
                Adicionar funcionário
              </a>
            }
          </div>
        } @else {
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th (click)="sort('firstName')" class="sortable">
                    Nome
                    @if (currentSort === 'firstName') {
                      <span>{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
                    }
                  </th>
                  <th>E-mail</th>
                  <th>Cargo</th>
                  <th>Departamento</th>
                  <th (click)="sort('hireDate')" class="sortable">
                    Contratado em
                    @if (currentSort === 'hireDate') {
                      <span>{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
                    }
                  </th>
                  <th>Status</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                @for (emp of page()?.content; track emp.id) {
                  <tr>
                    <td>
                      <div class="employee-name">
                        <div class="avatar">{{ emp.firstName[0] }}{{ emp.lastName[0] }}</div>
                        <div>
                          <span class="font-medium">{{ emp.fullName }}</span>
                        </div>
                      </div>
                    </td>
                    <td class="text-muted text-sm">{{ emp.email }}</td>
                    <td class="text-sm">{{ emp.position || '—' }}</td>
                    <td class="text-sm">
                      @if (emp.departmentName) {
                        <span class="badge badge-info">{{ emp.departmentName }}</span>
                      } @else {
                        <span class="text-muted">—</span>
                      }
                    </td>
                    <td class="text-sm text-muted">
                      {{ emp.hireDate | date:'dd/MM/yyyy' }}
                    </td>
                    <td>
                      <span class="badge" [class]="emp.active ? 'badge-success' : 'badge-muted'">
                        {{ emp.active ? 'Ativo' : 'Inativo' }}
                      </span>
                    </td>
                    <td>
                      <div class="actions">
                        <a [routerLink]="['/employees', emp.id]" class="btn btn-secondary btn-sm">
                          Ver
                        </a>
                        @if (canManage()) {
                          <a [routerLink]="['/employees', emp.id, 'edit']" class="btn btn-secondary btn-sm">
                            ✏️
                          </a>
                          @if (emp.active) {
                            <button
                              class="btn btn-danger btn-sm"
                              (click)="toggleActive(emp)"
                              [disabled]="toggling() === emp.id"
                            >
                              Desativar
                            </button>
                          } @else {
                            <button
                              class="btn btn-success btn-sm"
                              (click)="toggleActive(emp)"
                              [disabled]="toggling() === emp.id"
                            >
                              Ativar
                            </button>
                          }
                        }
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <!-- Paginação -->
          <div class="pagination">
            <span class="pagination-info">
              Mostrando {{ pageStart() }}–{{ pageEnd() }} de {{ page()?.totalElements }} funcionários
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

    .filters-card {
      margin-bottom: 1rem;
      padding: 1rem;
    }

    .filters {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;

      select {
        padding: 0.5rem 0.75rem;
        border: 1px solid var(--color-border);
        border-radius: var(--radius-md);
        font-size: 0.875rem;
        background: white;
        color: var(--color-text);
        outline: none;
        cursor: pointer;

        &:focus { border-color: var(--color-primary); }
      }
    }

    .search-box {
      flex: 1;
      min-width: 200px;
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
      p    { font-size: 0.95rem; }
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

    .sortable {
      cursor: pointer;
      user-select: none;
      &:hover { color: var(--color-primary); }
    }

    .employee-name {
      display: flex;
      align-items: center;
      gap: 0.75rem;

      .avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: var(--color-primary-light);
        color: var(--color-primary);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.75rem;
        font-weight: 700;
        flex-shrink: 0;
      }
    }

    .actions {
      display: flex;
      gap: 0.375rem;
      flex-wrap: wrap;
    }

    .page-indicator {
      font-size: 0.875rem;
      color: var(--color-text-muted);
      padding: 0 0.5rem;
    }
  `]
})
export class EmployeeListComponent implements OnInit, OnDestroy {

  page        = signal<PageResponse<Employee> | null>(null);
  departments = signal<Department[]>([]);
  loading     = signal(true);
  toggling    = signal<number | null>(null);

  searchTerm         = '';
  selectedDepartment: number | null = null;
  currentPage  = 0;
  pageSize     = 10;
  currentSort  = 'firstName';
  sortDir: 'asc' | 'desc' = 'asc';

  private searchSubject = new Subject<string>();
  private destroy$      = new Subject<void>();

  readonly canManage = this.authService.isAdminOrManager;

  constructor(
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadDepartments();
    this.loadEmployees();

    // Debounce na busca — 400ms
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadEmployees();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadEmployees(): void {
    this.loading.set(true);
    this.employeeService.findAll({
      page:         this.currentPage,
      size:         this.pageSize,
      sort:         this.currentSort,
      direction:    this.sortDir,
      search:       this.searchTerm || undefined,
      departmentId: this.selectedDepartment ?? undefined
    }).subscribe({
      next: page => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  loadDepartments(): void {
    this.departmentService.findAllList().subscribe({
      next: depts => this.departments.set(depts)
    });
  }

  onSearch(term: string): void {
    this.searchSubject.next(term);
  }

  onDepartmentFilter(deptId: number | null): void {
    this.currentPage = 0;
    this.loadEmployees();
  }

  onPageSizeChange(size: number): void {
    this.currentPage = 0;
    this.loadEmployees();
  }

  sort(field: string): void {
    if (this.currentSort === field) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.currentSort = field;
      this.sortDir = 'asc';
    }
    this.currentPage = 0;
    this.loadEmployees();
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadEmployees();
  }

  toggleActive(emp: Employee): void {
    this.toggling.set(emp.id);
    const action$ = emp.active
      ? this.employeeService.deactivate(emp.id)
      : this.employeeService.activate(emp.id);

    action$.subscribe({
      next: updated => {
        const current = this.page();
        if (current) {
          this.page.set({
            ...current,
            content: current.content.map(e => e.id === updated.id ? updated : e)
          });
        }
        this.toggling.set(null);
      },
      error: () => this.toggling.set(null)
    });
  }

  pageStart(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  pageEnd(): number {
    const end = (this.currentPage + 1) * this.pageSize;
    return Math.min(end, this.page()?.totalElements ?? 0);
  }
}
