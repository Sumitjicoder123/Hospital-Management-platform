import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';
import { SupabaseService } from '../services/supabase.service';

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
  needsPhone = false;
  missingPhoneForm = { name: '', phone: '', gender: '', address: '', userId: null as number | null };

  loginForm = { email: '', password: '' };
  showLoginPassword = false;
  
  registerForm = { email: '', password: '', confirmPassword: '' };
  showRegisterPassword = false;
  resetEmailSent = false;

  constructor(
    private apiService: ApiService,
    private supabaseService: SupabaseService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  authListener: any;
  hasSynced = false;

  async ngOnInit() {
    this.authListener = this.supabaseService.onAuthStateChange((event, session) => {
      if (session && !this.hasSynced && (event === 'SIGNED_IN' || event === 'INITIAL_SESSION')) {
        this.hasSynced = true;
        this.syncUserWithBackend(session.access_token);
      }
    });

    // Check if we already have a session (e.g. returning from Google OAuth)
    const { data: { session } } = await this.supabaseService.getSession();
    if (session && !this.hasSynced) {
      this.hasSynced = true;
      this.syncUserWithBackend(session.access_token);
    }
  }

  ngOnDestroy() {
    if (this.authListener) {
      this.authListener.data?.subscription?.unsubscribe();
    }
  }

  async onGoogleSignIn() {
    this.loading = true;
    this.error = null;
    try {
      await this.supabaseService.signInWithGoogle();
      // the redirect will handle the rest
    } catch (e: any) {
      this.error = e.message;
      this.loading = false;
    }
  }

  async onEmailSignIn() {
    if (!this.loginForm.email || !this.loginForm.password) {
      this.error = 'Please enter email and password.';
      return;
    }
    this.loading = true;
    this.error = null;

    try {
      const { data, error } = await this.supabaseService.signInWithEmail(this.loginForm.email, this.loginForm.password);
      if (error) throw error;
      if (data.session) {
        this.syncUserWithBackend(data.session.access_token);
      }
    } catch (err: any) {
      this.loading = false;
      this.error = err.message || 'Invalid credentials.';
      this.cdr.detectChanges();
    }
  }

  async onEmailSignUp() {
    if (!this.registerForm.email || !this.registerForm.password || !this.registerForm.confirmPassword) {
      this.error = 'Please fill out all fields.';
      return;
    }
    
    // Validate email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.registerForm.email)) {
      this.error = 'Please enter a valid email address.';
      return;
    }

    if (this.registerForm.password.length < 6) {
      this.error = 'Password must be at least 6 characters long.';
      return;
    }
    
    if (this.registerForm.password !== this.registerForm.confirmPassword) {
      this.error = 'Passwords do not match.';
      return;
    }

    this.loading = true;
    this.error = null;

    try {
      const { data, error } = await this.supabaseService.signUp(this.registerForm.email, this.registerForm.password);
      if (error) throw error;
      
      if (data.session) {
        // Email confirmation disabled, sign in immediately
        this.syncUserWithBackend(data.session.access_token);
      } else {
        // Email confirmation required
        this.error = 'Success! Please check your inbox to confirm your email, then sign in.';
        this.activeTab = 'login';
        this.loading = false;
        this.cdr.detectChanges();
      }
    } catch (err: any) {
      this.loading = false;
      this.error = err.message || 'Registration failed.';
      this.cdr.detectChanges();
    }
  }

  async onForgotPassword() {
    if (!this.loginForm.email) {
      this.error = 'Please enter your email to reset password.';
      return;
    }
    
    this.loading = true;
    this.error = null;
    this.resetEmailSent = false;
    
    try {
      const { error } = await this.supabaseService.resetPasswordForEmail(this.loginForm.email);
      if (error) throw error;
      
      this.resetEmailSent = true;
      this.loading = false;
      this.cdr.detectChanges();
    } catch (err: any) {
      this.loading = false;
      this.error = err.message || 'Failed to send reset link.';
      this.cdr.detectChanges();
    }
  }

  private syncUserWithBackend(token: string) {
    localStorage.setItem('auth_token', token); // Use Supabase token as Bearer initially
    this.apiService.getCurrentUser().subscribe({
      next: (res: any) => {
        // Save the new backend-issued JWT
        if (res.token) {
          localStorage.setItem('auth_token', res.token);
        }
        
        localStorage.setItem('auth_user', JSON.stringify({
          id: res.userId,
          name: res.name,
          email: res.email,
          phone: res.phone,
          role: res.role,
          hospitalId: res.hospitalId,
          doctorId: res.doctorId,
          profilePicture: res.profilePicture,
          profileCompleted: res.profileCompleted
        }));
        
        // If profile is not completed, redirect to a profile completion state
        if (res.profileCompleted === false) {
          this.loading = false;
          this.needsPhone = true;
          this.missingPhoneForm.userId = res.userId;
          this.missingPhoneForm.name = res.name || '';
          this.missingPhoneForm.phone = res.phone || '';
          this.cdr.detectChanges(); // Force UI update
          return;
        }
        
        this.router.navigate(['/dashboard']);
      },
      error: (err: any) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to sync with backend server.';
        this.cdr.detectChanges();
      }
    });
  }

  onSaveProfile(): void {
    if (!/^\d{10}$/.test(this.missingPhoneForm.phone)) {
      this.error = 'Phone number must be exactly 10 digits.';
      return;
    }
    if (!this.missingPhoneForm.name || !this.missingPhoneForm.gender || !this.missingPhoneForm.address) {
      this.error = 'Please fill out all fields.';
      return;
    }
    
    this.loading = true;
    this.error = null;

    this.apiService.completeProfile({
      name: this.missingPhoneForm.name,
      phone: this.missingPhoneForm.phone,
      gender: this.missingPhoneForm.gender,
      address: this.missingPhoneForm.address
    }).subscribe({
      next: (res: any) => {
        console.log('completeProfile success:', res);
        if (res.token) {
          localStorage.setItem('auth_token', res.token);
        }
        localStorage.setItem('auth_user', JSON.stringify({
          id: res.userId,
          name: res.name,
          email: res.email,
          phone: res.phone,
          role: res.role,
          hospitalId: res.hospitalId,
          doctorId: res.doctorId,
          profilePicture: res.profilePicture,
          profileCompleted: res.profileCompleted
        }));
        
        this.router.navigate(['/dashboard']).then(success => {
            console.log('router.navigate /dashboard success:', success);
            if (!success) {
                this.loading = false;
                this.error = 'Navigation to dashboard failed. Check console.';
                this.cdr.detectChanges();
            }
        }).catch(err => {
            console.error('router.navigate error:', err);
            this.loading = false;
            this.error = 'Navigation error: ' + err.message;
            this.cdr.detectChanges();
        });
      },
      error: (err: any) => {
        console.error('completeProfile error:', err);
        this.loading = false;
        this.error = err.error?.message || 'Failed to update profile.';
        this.cdr.detectChanges();
      }
    });
  }

  async onSignOut() {
    this.loading = true;
    try {
      await this.supabaseService.signOut();
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      this.needsPhone = false;
      this.activeTab = 'login';
      this.error = null;
    } catch (e) {
      console.error(e);
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }
}
