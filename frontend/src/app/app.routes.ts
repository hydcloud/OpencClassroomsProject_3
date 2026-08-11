import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { Files } from './features/files/files/files';

export const routes: Routes = [
  {
    path: '',
    component: Files
  },
  {
    path: 'login',
    component: Login
  },
  {
    path: 'register',
    component: Register
  }
];
