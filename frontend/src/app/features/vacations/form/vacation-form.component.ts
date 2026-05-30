import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { VacationService } from '@core/services/vacation.service';
import { EmployeeService } from '@core/services/employee.service';
import { Employee } from '@core/models';

@Component({
  selector: 'app-vacation-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">

      <div class="page-header">
        <a routerLink="/vacations" class="back-link">← Férias</a>
        <h1>Solicitar Férias</h1>
        @if (employee()) {
          <p class="text-muted">Para: <strong>{{ employee()!.fullName }}</strong></p>
        }
      </div>

      <div class="card form-card">

        @if (errorMsg()) {
          <div class="alert alert-error">{{ errorMsg() }}</div>
        }

        <form [formGroup]="form" (ngSubmit)="onSubmit()">

          <div class="form-grid">
            <div class="form-group">
              <label for="startDate">Data de Início *</label>
              <input
                id="startDate"
                type="date"
                formControlName="startDate"
                [min]="tomorrow()"
                [class.error]="isInvalid('startDate')"
              />
              @if (isInvalid('startDate')) {
                <span class="error-msg">Data de início é obrigatória</span>
              }
            </div>

            <div class="form-group">
              <label for="endDate">Data de Fim *</label>
              <input
                id="endDate"
                type="date"
                formControlName="endDate"
                [min]="form.get('startDate')?.value || tomorrow()"
                [class.error]="isInvalid('endDate')"
              />
              @if (isInvalid('endDate')) {
                <span class="error-msg">Data de fim é obrigatória</span>
              }
            </div>
          </div>

          @if (totalDays() > 0) {
            <div class="days-preview">
              <span class="badge badge-info">{{ totalDays() }} dia(s) de férias</span>
            </div>
          }

          <div class="form-group">
            <label for="reason">Motivo (opcional)</label>
            <textarea
              id="reason"
              formControlName="reason"
              placeholder="Descreva o motivo da solicitação..."
              rows="4"
            ></textarea>
          </div>

          <div class="form-footer">
            <a routerLink="/vacations" class="btn btn-secondary">Cancelar</a>
            <button
              type="submit"
              class="btn btn-primary"
              [disabled]="saving()"
            >
              @if (saving()) {
                <span class="spinner"></span> Enviando...
              } @else {
                Enviar Solicitação
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

      h1 { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.25rem; }
    }

    .form-card { max-width: 560px; }

    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0 1.5rem;

      @media (max-width: 480px) { grid-template-columns: 1fr; }
    }

    .days-preview {
      margin-bottom: 1rem;
    }

    .form-footer {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      padding-top: 1.5rem;
      border-top: 1px solid var(--color-border);
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
export class VacationFormComponent implements OnInit {

  form     : FormGroup;
  saving   = signal(false);
  errorMsg = signal('');
  employee = signal<Employee | null>(null);

  private employeeId!: number;

  constructor(
    private fb: FormBuilder,
    private vacationService: VacationService,
    private employeeService: EmployeeService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      startDate: ['', Validators.required],
      endDate:   ['', Validators.required],
      reason:    ['', Validators.maxLength(500)]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('employeeId');
    if (!id) { this.router.navigate(['/vacations']); return; }

    this.employeeId = +id;
    this.employeeService.findById(+id).subscribe({
      next: emp => this.employee.set(emp),
      error: ()  => this.router.navigate(['/vacations'])
    });
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const { startDate, endDate } = this.form.value;
    if (new Date(endDate) < new Date(startDate)) {
      this.errorMsg.set('A data de fim não pode ser anterior à data de início.');
      return;
    }

    this.saving.set(true);
    this.errorMsg.set('');

    this.vacationService.create(this.employeeId, this.form.value).subscribe({
      next: () => this.router.navigate(['/vacations']),
      error: (err) => {
        this.saving.set(false);
        this.errorMsg.set(err.error?.message ?? 'Erro ao enviar solicitação.');
      }
    });
  }

  tomorrow(): string {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().split('T')[0];
  }

  totalDays(): number {
    const { startDate, endDate } = this.form.value;
    if (!startDate || !endDate) return 0;
    const diff = new Date(endDate).getTime() - new Date(startDate).getTime();
    return diff >= 0 ? Math.floor(diff / 86400000) + 1 : 0;
  }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c?.touched);
  }
}
