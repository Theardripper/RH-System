import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/auth.guard';

export const EMPLOYEES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./list/employee-list.component').then(m => m.EmployeeListComponent)
  },
  {
    path: 'new',
    canActivate: [roleGuard('ADMIN', 'MANAGER')],
    loadComponent: () =>
      import('./form/employee-form.component').then(m => m.EmployeeFormComponent)
  },
  {
    path: ':id/edit',
    canActivate: [roleGuard('ADMIN', 'MANAGER')],
    loadComponent: () =>
      import('./form/employee-form.component').then(m => m.EmployeeFormComponent)
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./detail/employee-detail.component').then(m => m.EmployeeDetailComponent)
  }
];
