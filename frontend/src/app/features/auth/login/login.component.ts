import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-wrapper">

      <div class="auth-left">
        <div class="auth-brand">
          <div class="brand-icon">HR</div>
          <h1>HR System</h1>
          <p>Gestão de Recursos Humanos</p>
        </div>
        <div class="auth-features">
          <div class="feature">
            <span class="feature-icon">👥</span>
            <span>Gestão de Funcionários</span>
          </div>
          <div class="feature">
            <span class="feature-icon">🏢</span>
            <span>Controle de Departamentos</span>
          </div>
          <div class="feature">
            <span class="feature-icon">🏖️</span>
            <span>Solicitações de Férias</span>
          </div>
        </div>
      </div>

      <div class="auth-right">
        <div class="auth-card">
          <div class="auth-header">
            <h2>Bem-vindo de volta</h2>
            <p>Faça login para continuar</p>
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
                placeholder="Digite seu usuário"
                [class.error]="isInvalid('username')"
                autocomplete="username"
              />
              @if (isInvalid('username')) {
                <span class="error-msg">Usuário é obrigatório</span>
              }
            </div>

            <div class="form-group">
              <label for="password">Senha</label>
              <div class="input-password">
                <input
                  id="password"
                  [type]="showPassword() ? 'text' : 'password'"
                  formControlName="password"
                  placeholder="Digite sua senha"
                  [class.error]="isInvalid('password')"
                  autocomplete="current-password"
                />
                <button
                  type="button"
                  class="toggle-password"
                  (click)="showPassword.set(!showPassword())"
                >
                  {{ showPassword() ? '🙈' : '👁️' }}
                </button>
              </div>
              @if (isInvalid('password')) {
                <span class="error-msg">Senha é obrigatória</span>
              }
            </div>

            <button
              type="submit"
              class="btn btn-primary w-full"
              [disabled]="loading()"
            >
              @if (loading()) {
                <span class="spinner"></span> Entrando...
              } @else {
                Entrar
              }
            </button>
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
      margin-bottom: 3rem;

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
        backdrop-filter: blur(10px);
      }

      h1 {
        font-size: 2rem;
        font-weight: 700;
        margin-bottom: 0.5rem;
      }

      p {
        color: rgba(255,255,255,0.7);
        font-size: 1rem;
      }
    }

    .auth-features {
      display: flex;
      flex-direction: column;
      gap: 1rem;

      .feature {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        font-size: 0.95rem;
        color: rgba(255,255,255,0.85);

        .feature-icon {
          font-size: 1.25rem;
        }
      }
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
      max-width: 420px;
      background: white;
      border-radius: 1rem;
      padding: 2.5rem;
      box-shadow: 0 10px 25px rgba(0,0,0,0.08);
    }

    .auth-header {
      margin-bottom: 2rem;
      text-align: center;

      h2 {
        font-size: 1.5rem;
        font-weight: 700;
        color: #1e293b;
        margin-bottom: 0.5rem;
      }

      p {
        color: #64748b;
        font-size: 0.9rem;
      }
    }

    .input-password {
      position: relative;

      input { width: 100%; padding-right: 2.5rem; }

      .toggle-password {
        position: absolute;
        right: 0.75rem;
        top: 50%;
        transform: translateY(-50%);
        background: none;
        border: none;
        cursor: pointer;
        font-size: 1rem;
        padding: 0;
      }
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
export class LoginComponent {

  form: FormGroup;
  loading  = signal(false);
  errorMsg = signal('');
  showPassword = signal(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set('');

    this.authService.login(this.form.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(
          err.status === 401
            ? 'Usuário ou senha inválidos'
            : 'Erro ao fazer login. Tente novamente.'
        );
      }
    });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control?.touched);
  }
}
