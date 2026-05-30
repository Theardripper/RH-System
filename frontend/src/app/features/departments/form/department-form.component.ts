import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { DepartmentService } from '@core/services/department.service';

@Component({
  selector: 'app-department-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">

      <div class="page-header">
        <a routerLink="/departments" class="back-link">← Departamentos</a>
        <h1>{{ isEditing() ? 'Editar Departamento' : 'Novo Departamento' }}</h1>
      </div>

      <div class="card form-card">

        @if (errorMsg()) {
          <div class="alert alert-error">{{ errorMsg() }}</div>
        }

        <form [formGroup]="form" (ngSubmit)="onSubmit()">

          <div class="form-group">
            <label for="name">Nome do Departamento *</label>
            <input
              id="name"
              type="text"
              formControlName="name"
              placeholder="Ex: Tecnologia, Financeiro..."
              [class.error]="isInvalid('name')"
            />
            @if (isInvalid('name')) {
              <span class="error-msg">Nome é obrigatório (mínimo 2 caracteres)</span>
            }
          </div>

          <div class="form-group">
            <label for="description">Descrição</label>
            <textarea
              id="description"
              formControlName="description"
              placeholder="Descreva as responsabilidades deste departamento..."
              rows="4"
            ></textarea>
          </div>

          <div class="form-footer">
            <a routerLink="/departments" class="btn btn-secondary">Cancelar</a>
            <button
              type="submit"
              class="btn btn-primary"
              [disabled]="saving()"
            >
              @if (saving()) {
                <span class="spinner"></span> Salvando...
              } @else {
                {{ isEditing() ? 'Salvar Alterações' : 'Criar Departamento' }}
              }
            </button>
          </div>

        </form>
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

    .form-card { max-width: 560px; }

    .form-footer {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      padding-top: 1.5rem;
      border-top: 1px solid var(--color-border);
      margin-top: 0.5rem;
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
export class DepartmentFormComponent implements OnInit {

  form      : FormGroup;
  saving    = signal(false);
  errorMsg  = signal('');
  isEditing = signal(false);

  private deptId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private departmentService: DepartmentService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      name:        ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      description: ['', Validators.maxLength(255)]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditing.set(true);
      this.deptId = +id;
      this.departmentService.findById(+id).subscribe({
        next: dept => this.form.patchValue({ name: dept.name, description: dept.description }),
        error: ()  => this.router.navigate(['/departments'])
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.saving.set(true);
    this.errorMsg.set('');

    const action$ = this.isEditing() && this.deptId
      ? this.departmentService.update(this.deptId, this.form.value)
      : this.departmentService.create(this.form.value);

    action$.subscribe({
      next: () => this.router.navigate(['/departments']),
      error: (err) => {
        this.saving.set(false);
        this.errorMsg.set(err.error?.message ?? 'Erro ao salvar departamento.');
      }
    });
  }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c?.touched);
  }
}
