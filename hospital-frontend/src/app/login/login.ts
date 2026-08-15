import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  activeTab: 'login' | 'register' = 'login';
  error: string | null = null;
  loading = false;

  loginForm = { email: '', password: '' };
  showLoginPassword = false;

  registerForm = {
    name: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: ''
  };
  showRegisterPassword = false;
  showConfirmPassword = false;

  constructor(
    private apiService: ApiService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onLogin(): void {
    if (!this.loginForm.email || !this.loginForm.password) {
      this.error = 'Please enter email and password.';
      return;
    }

    this.loading = true;
    this.error = null;

    this.apiService.login(this.loginForm).subscribe({
      next: (res) => {
        localStorage.setItem('auth_token', res.token);
        localStorage.setItem('auth_user', JSON.stringify({
          id: res.userId,
          name: res.name,
          email: res.email,
          phone: res.phone,
          role: res.role,
          hospitalId: res.hospitalId,
          doctorId: res.doctorId
        }));
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || err.error || 'Invalid credentials. Please try again.';
        this.cdr.markForCheck();
      }
    });
  }

  onRegister(): void {
    if (!this.registerForm.name || !this.registerForm.email || !this.registerForm.password) {
      this.error = 'Please fill in all required fields.';
      return;
    }

    if (this.registerForm.password !== this.registerForm.confirmPassword) {
      this.error = 'Passwords do not match.';
      return;
    }

    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
    if (!passwordRegex.test(this.registerForm.password)) {
      this.error = 'Password must be at least 8 characters long and contain at least one letter and one number.';
      return;
    }

    const phoneRegex = /^\d{10}$/;
    if (!phoneRegex.test(this.registerForm.phone)) {
      this.error = 'Phone number must be exactly 10 digits.';
      return;
    }

    this.loading = true;
    this.error = null;

    const payload = {
      name: this.registerForm.name,
      email: this.registerForm.email,
      phone: this.registerForm.phone,
      password: this.registerForm.password,
      role: 'PATIENT',
      hospitalId: null
    };

    this.apiService.register(payload).subscribe({
      next: (res) => {
        localStorage.setItem('auth_token', res.token);
        localStorage.setItem('auth_user', JSON.stringify({
          id: res.userId,
          name: res.name,
          email: res.email,
          phone: res.phone,
          role: res.role,
          hospitalId: res.hospitalId,
          doctorId: res.doctorId
        }));
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || err.error || 'Registration failed. Email may already be in use.';
        this.cdr.markForCheck();
      }
    });
  }
}
