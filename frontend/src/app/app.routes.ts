import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from '@core/guards/auth.guard';

export const routes: Routes = [
  // Redireciona raiz para dashboard
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },

  // Auth — acessível apenas sem login
  {
    path: 'auth',
    canActivate: [guestGuard],
    loadChildren: () =>
      import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },

  // Área protegida — exige autenticação
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/components/layout/layout.component').then(m => m.LayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'employees',
        loadChildren: () =>
          import('./features/employees/employees.routes').then(m => m.EMPLOYEES_ROUTES)
      },
      {
        path: 'departments',
        canActivate: [roleGuard('ADMIN')],
        loadChildren: () =>
          import('./features/departments/departments.routes').then(m => m.DEPARTMENTS_ROUTES)
      },
      {
        path: 'vacations',
        loadChildren: () =>
          import('./features/vacations/vacations.routes').then(m => m.VACATIONS_ROUTES)
      }
    ]
  },

  // Wildcard
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
