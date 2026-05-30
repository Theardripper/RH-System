import { Component, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '@core/services/auth.service';
import { DepartmentService } from '@core/services/department.service';
import { EmployeeService } from '@core/services/employee.service';
import { Employee, UserRole } from '@core/models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-wrapper">

      <div class="auth-left">
        <div class="auth-brand">
          <div class="brand-icon">HR</div>
          <h1>HR System</h1>
          <p>Registrar novo usuário</p>
        </div>
      </div>

      <div class="auth-right">
        <div class="auth-card">
          <div class="auth-header">
            <h2>Novo Usuário</h2>
            <p>Preencha os dados para criar a conta</p>
          </div>

          @if (errorMsg()) {
            <div class="alert alert-error">{{ errorMsg() }}</div>
          }

          <form [formGroup]="form" (ngSubmit)="onSubmit()">

            <div class="form-group">
              <label for="username">Usuário</label>
              <input
                id="username"
                type="text"
                formControlName="username"
                placeholder="Nome de usuário"
                [class.error]="isInvalid('username')"
              />
              @if (isInvalid('username')) {
                <span class="error-msg">Mínimo 3 caracteres</span>
              }
            </div>

            <div class="form-group">
              <label for="email">E-mail</label>
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
              <label for="password">Senha</label>
              <input
                id="password"
                type="password"
                formControlName="password"
                placeholder="Mínimo 8 caracteres"
                [class.error]="isInvalid('password')"
              />
              @if (isInvalid('password')) {
                <span class="error-msg">
                  Mínimo 8 caracteres com maiúscula, minúscula, número e símbolo
                </span>
              }
            </div>

            <div class="form-group">
              <label for="role">Perfil</label>
              <select id="role" formControlName="role">
                <option value="EMPLOYEE">Funcionário</option>
                <option value="MANAGER">Gerente</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </div>

            <div class="form-group">
              <label for="employeeId">Funcionário vinculado (opcional)</label>
              <select id="employeeId" formControlName="employeeId">
                <option [value]="null">— Nenhum —</option>
                @for (emp of employees(); track emp.id) {
                  <option [value]="emp.id">{{ emp.fullName }} ({{ emp.email }})</option>
                }
              </select>
            </div>

            <div class="form-actions">
              <a routerLink="/auth/login" class="btn btn-secondary">Cancelar</a>
              <button
                type="submit"
                class="btn btn-primary"
                [disabled]="loading()"
              >
                @if (loading()) {
                  <span class="spinner"></span> Registrando...
                } @else {
                  Registrar
                }
              </button>
            </div>

          </form>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .auth-wrapper {
      display: flex;
      min-height: 100vh;
    }

    .auth-left {
      flex: 1;
      background: linear-gradient(135deg, #1e293b 0%, #2563eb 100%);
      display: flex;
      flex-direction: column;
      justify-content: center;
      padding: 3rem;
      color: white;

      @media (max-width: 768px) { display: none; }
    }

    .auth-brand {
      .brand-icon {
        width: 64px;
        height: 64px;
        background: rgba(255,255,255,0.2);
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        font-weight: 700;
        margin-bottom: 1rem;
      }

      h1 { font-size: 2rem; font-weight: 700; margin-bottom: 0.5rem; }
      p  { color: rgba(255,255,255,0.7); }
    }

    .auth-right {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
      background: #f8fafc;
    }

    .auth-card {
      width: 100%;
      max-width: 480px;
      background: white;
      border-radius: 1rem;
      padding: 2.5rem;
      box-shadow: 0 10px 25px rgba(0,0,0,0.08);
    }

    .auth-header {
      margin-bottom: 2rem;
      text-align: center;

      h2 { font-size: 1.5rem; font-weight: 700; color: #1e293b; margin-bottom: 0.5rem; }
      p  { color: #64748b; font-size: 0.9rem; }
    }

    .form-actions {
      display: flex;
      gap: 0.75rem;
      margin-top: 1.5rem;

      .btn { flex: 1; justify-content: center; }
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

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class RegisterComponent implements OnInit {

  form: FormGroup;
  loading   = signal(false);
  errorMsg  = signal('');
  employees = signal<Employee[]>([]);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private employeeService: EmployeeService,
    private router: Router
  ) {
    this.form = this.fb.group({
      username:   ['', [Validators.required, Validators.minLength(3)]],
      email:      ['', [Validators.required, Validators.email]],
      password:   ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/)
      ]],
      role:       ['EMPLOYEE'],
      employeeId: [null]
    });
  }

  ngOnInit(): void {
    this.employeeService.findAll({ size: 100 }).subscribe({
      next: page => this.employees.set(page.content)
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set('');

    const value = this.form.value;
    const request = {
      ...value,
      employeeId: value.employeeId || null
    };

    this.authService.register(request).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(
          err.error?.message ?? 'Erro ao registrar usuário.'
        );
      }
    });
  }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c?.touched);
  }
}
