import { Routes } from '@angular/router';

export const VACATIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./list/vacation-list.component').then(m => m.VacationListComponent)
  },
  {
    path: 'new/:employeeId',
    loadComponent: () =>
      import('./form/vacation-form.component').then(m => m.VacationFormComponent)
  }
];
