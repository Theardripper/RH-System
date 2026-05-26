import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { UserRole } from '@core/models';

/**
 * Guard de autenticação — redireciona para login se não autenticado.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/auth/login']);
  return false;
};

/**
 * Guard de role — redireciona para dashboard se não tiver permissão.
 * Uso: canActivate: [roleGuard('ADMIN', 'MANAGER')]
 */
export const roleGuard = (...roles: UserRole[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router      = inject(Router);

    if (authService.hasRole(...roles)) {
      return true;
    }

    router.navigate(['/dashboard']);
    return false;
  };
};

/**
 * Guard de redirecionamento — se já logado, vai para dashboard.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  if (!authService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/dashboard']);
  return false;
};
