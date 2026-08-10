import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { UserService } from '../user.service';
import { RegisterRequest } from '../../../models/register-request';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {

  errorMessage = '';
  isLoading = false;

  registerForm;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly userService: UserService,
    private readonly router: Router
  ) {
    this.registerForm = this.formBuilder.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const registerRequest: RegisterRequest =
      this.registerForm.getRawValue();

    this.isLoading = true;
    this.errorMessage = '';

    this.userService.register(registerRequest).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/login']);
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Impossible de créer le compte.';
      }
    });
  }
}