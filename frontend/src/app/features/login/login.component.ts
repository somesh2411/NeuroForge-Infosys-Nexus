import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  credentials = {
    username: '',
    password: '',
    verificationCode: ''
  };
  passChecks = {
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    special: false
  };
  errorMessage = '';
  loading = false;

  onPasswordChange(): void {
    const p = this.credentials.password || '';
    this.passChecks.length = p.length >= 12;
    this.passChecks.uppercase = /[A-Z]/.test(p);
    this.passChecks.lowercase = /[a-z]/.test(p);
    this.passChecks.number = /[0-9]/.test(p);
    this.passChecks.special = /[!@#$%^&*(),.?":{}|<>]/.test(p);
  }

  get isPasswordValid(): boolean {
    return this.passChecks.length &&
           this.passChecks.uppercase &&
           this.passChecks.lowercase &&
           this.passChecks.number &&
           this.passChecks.special;
  }

  async onSubmit(): Promise<void> {
    this.errorMessage = '';
    
    if (!this.credentials.username || !this.credentials.password || !this.credentials.verificationCode) {
      this.errorMessage = 'All fields are required. Please fill out Username, Password, and Verification Code.';
      this.snackBar.open(this.errorMessage, 'Close', { duration: 4000 });
      return;
    }

    if (!this.isPasswordValid) {
      this.errorMessage = 'Password does not meet security standards. Please satisfy all requirements.';
      this.snackBar.open(this.errorMessage, 'Close', { duration: 4000 });
      return;
    }

    this.loading = true;
    try {
      await this.authService.loginWithCredentials(
        this.credentials.username,
        this.credentials.password,
        this.credentials.verificationCode
      );
      this.snackBar.open('Welcome back to NeuroForge!', 'Close', { duration: 3000 });
      this.router.navigate(['/dashboard']);
    } catch (err: any) {
      this.errorMessage = err.message || 'Login failed. Please check your credentials.';
      this.snackBar.open(this.errorMessage, 'Close', { duration: 4000 });
    } finally {
      this.loading = false;
    }
  }
}
