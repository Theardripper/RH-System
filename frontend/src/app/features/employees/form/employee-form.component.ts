import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { EmployeeService } from '@core/services/employee.service';
import { DepartmentService } from '@core/services/department.service';
import { Department } from '@core/models';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">

      <div class="page-header">
        <div>
          <a routerLink="/employees" class="back-link">← Funcionários</a>
          <h1>{{ isEditing() ? 'Editar Funcionário' : 'Novo Funcionário' }}</h1>
        </div>
      </div>

      <div class="card form-card">

        @if (errorMsg()) {
          <div class="alert alert-error">{{ errorMsg() }}</div>
        }

        @if (loadingData()) {
          <div class="loading-state">
            <div class="spinner-lg"></div>
          </div>
        } @else {
          <form [formGroup]="form" (ngSubmit)="onSubmit()">

            <div class="form-section">
              <h3>Dados Pessoais</h3>
              <div class="form-grid">

                <div class="form-group">
                  <label for="firstName">Nome *</label>
                  <input
                    id="firstName"
                    type="text"
                    formControlName="firstName"
                    placeholder="Nome"
                    [class.error]="isInvalid('firstName')"
                  />
                  @if (isInvalid('firstName')) {
                    <span class="error-msg">Nome é obrigatório</span>
                  }
                </div>

                <div class="form-group">
                  <label for="lastName">Sobrenome *</label>
                  <input
                    id="lastName"
                    type="text"
                    formControlName="lastName"
                    placeholder="Sobrenome"
                    [class.error]="isInvalid('lastName')"
                  />
                  @if (isInvalid('lastName')) {
                    <span class="error-msg">Sobrenome é obrigatório</span>
                  }
                </div>

                <div class="form-group">
                  <label for="email">E-mail *</label>
                  <input
                    id="email"
                    type="email"
                    formControlName="email"
                    placeholder="email@empresa.com"
                    [class.error]="isInvalid('email')"
                  />
                  @if (isInvalid('email')) {
                    <span class="error-msg">E-mail inválido</span>
                  }
                </div>

                <div class="form-group">
                  <label for="phone">Telefone</label>
                  <input
                    id="phone"
                    type="text"
                    formControlName="phone"
                    placeholder="(00) 00000-0000"
                  />
                </div>

              </div>
            </div>

            <div class="form-section">
              <h3>Dados Profissionais</h3>
              <div class="form-grid">

                <div class="form-group">
                  <label for="position">Cargo</label>
                  <input
                    id="position"
                    type="text"
                    formControlName="position"
                    placeholder="Ex: Desenvolvedor, Analista..."
                  />
                </div>

                <div class="form-group">
                  <label for="departmentId">Departamento</label>
                  <select id="departmentId" formControlName="departmentId">
                    <option [ngValue]="null">— Sem departamento —</option>
                    @for (dept of departments(); track dept.id) {
                      <option [ngValue]="dept.id">{{ dept.name }}</option>
                    }
                  </select>
                </div>

                <div class="form-group">
                  <label for="hireDate">Data de Contratação *</label>
                  <input
                    id="hireDate"
                    type="date"
                    formControlName="hireDate"
                    [class.error]="isInvalid('hireDate')"
                  />
                  @if (isInvalid('hireDate')) {
                    <span class="error-msg">Data de contratação é obrigatória</span>
                  }
                </div>

                <div class="form-group">
                  <label for="salary">Salário (R$) *</label>
                  <input
                    id="salary"
                    type="number"
                    formControlName="salary"
                    placeholder="0,00"
                    step="0.01"
                    min="0"
                    [class.error]="isInvalid('salary')"
                  />
                  @if (isInvalid('salary')) {
                    <span class="error-msg">Salário deve ser maior que zero</span>
                  }
                </div>

              </div>
            </div>

            <div class="form-footer">
              <a routerLink="/employees" class="btn btn-secondary">Cancelar</a>
              <button
                type="submit"
                class="btn btn-primary"
                [disabled]="saving()"
              >
                @if (saving()) {
                  <span class="spinner"></span>
                  Salvando...
                } @else {
                  {{ isEditing() ? 'Salvar Alterações' : 'Criar Funcionário' }}
                }
              </button>
            </div>

          </form>
        }

      </div>
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 1.25rem;

      .back-link {
        font-size: 0.875rem;
        color: var(--color-text-muted);
        text-decoration: none;
        display: inline-block;
        margin-bottom: 0.5rem;
        &:hover { color: var(--color-primary); }
      }

      h1 { font-size: 1.5rem; font-weight: 700; }
    }

    .form-card { max-width: 860px; }

    .form-section {
      margin-bottom: 2rem;

      h3 {
        font-size: 0.875rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: var(--color-text-muted);
        margin-bottom: 1rem;
        padding-bottom: 0.5rem;
        border-bottom: 1px solid var(--color-border);
      }
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 0 1.5rem;
    }

    .form-footer {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      padding-top: 1.5rem;
      border-top: 1px solid var(--color-border);
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

    .spinner {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255,255,255,0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.7s linear infinite;
      display: inline-block;
    }

    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class EmployeeFormComponent implements OnInit {

  form: FormGroup;
  departments = signal<Department[]>([]);
  loadingData = signal(false);
  saving      = signal(false);
  errorMsg    = signal('');
  isEditing   = signal(false);

  private employeeId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      firstName:    ['', Validators.required],
      lastName:     ['', Validators.required],
      email:        ['', [Validators.required, Validators.email]],
      phone:        [''],
      position:     [''],
      departmentId: [null],
      hireDate:     ['', Validators.required],
      salary:       [null, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.loadDepartments();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditing.set(true);
      this.employeeId = +id;
      this.loadEmployee(+id);
    }
  }

  loadDepartments(): void {
    this.departmentService.findAllList().subscribe({
      next: depts => this.departments.set(depts)
    });
  }

  loadEmployee(id: number): void {
    this.loadingData.set(true);
    this.employeeService.findById(id).subscribe({
      next: emp => {
        this.form.patchValue({
          firstName:    emp.firstName,
          lastName:     emp.lastName,
          email:        emp.email,
          phone:        emp.phone,
          position:     emp.position,
          departmentId: emp.departmentId,
          hireDate:     emp.hireDate,
          salary:       emp.salary
        });
        this.loadingData.set(false);
      },
      error: () => {
        this.router.navigate(['/employees']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMsg.set('');

    const request = this.form.value;
    const action$ = this.isEditing() && this.employeeId
      ? this.employeeService.update(this.employeeId, request)
      : this.employeeService.create(request);

    action$.subscribe({
      next: () => this.router.navigate(['/employees']),
      error: (err) => {
        this.saving.set(false);
        this.errorMsg.set(err.error?.message ?? 'Erro ao salvar funcionário.');
      }
    });
  }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c?.touched);
  }
}
