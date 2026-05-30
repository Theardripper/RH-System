import { Routes } from '@angular/router';

export const DEPARTMENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./list/department-list.component').then(m => m.DepartmentListComponent)
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./form/department-form.component').then(m => m.DepartmentFormComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./form/department-form.component').then(m => m.DepartmentFormComponent)
  }
];
