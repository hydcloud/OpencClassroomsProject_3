import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Files } from './features/files/files/files';

export const routes: Routes = [
  {
    path: 'login',
    component: Login
  },
  {
    path: 'files',
    component: Files
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];
