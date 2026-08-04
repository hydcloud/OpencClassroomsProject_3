import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../auth.service';
import { LoginRequest } from '../../../models/login-request';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})

export class Login {
  errorMessage = '';
  isLoading = false;

  loginForm;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.loginForm = this.formBuilder.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const loginRequest: LoginRequest = this.loginForm.getRawValue();

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(loginRequest).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/files']);
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Adresse email ou mot de passe incorrect.';
      }
    });
  }
}