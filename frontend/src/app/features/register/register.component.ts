import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  user = {
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    role: '',
    roleCode: ''
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

  roles = [
    { value: 'ORGANIZATION_OWNER', label: 'Organization Owner' },
    { value: 'TEAM_LEAD', label: 'Team Lead' },
    { value: 'DEVELOPER', label: 'Software Developer' },
    { value: 'QA', label: 'Quality Assurance' },
    { value: 'STAKEHOLDER', label: 'Stakeholder' }
  ];

  onPasswordChange(): void {
    const p = this.user.password || '';
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

    if (!this.user.username || !this.user.email || !this.user.password || !this.user.role || !this.user.roleCode || !this.user.firstName || !this.user.lastName) {
      this.errorMessage = 'All fields are required. Please fill out First/Last names, Username, Email, Password, Role, and Role Code.';
      this.snackBar.open(this.errorMessage, 'Close', { duration: 4000 });
      return;
    }

    if (!this.user.email.endsWith('@neuroforge.com')) {
      this.errorMessage = "Email address must end with '@neuroforge.com'.";
      this.snackBar.open(this.errorMessage, 'Close', { duration: 4000 });
      return;
    }

    if (!this.isPasswordValid) {
      this.errorMessage = 'Password does not meet security standards. Please satisfy all requirements.';
      this.snackBar.open(this.errorMessage, 'Close', { duration: 4000 });
      return;
    }

    // Role Code Verification Mapping
    let expectedCode = '';
    const role = this.user.role;
    if (role === 'ORGANIZATION_OWNER') expectedCode = 'OW0621';
    else if (role === 'TEAM_LEAD') expectedCode = 'TL0621';
    else if (role === 'DEVELOPER') expectedCode = 'SD0621';
    else if (role === 'QA') expectedCode = 'QA0621';
    else if (role === 'STAKEHOLDER') expectedCode = 'S0621';

    if (this.user.roleCode !== expectedCode) {
      this.errorMessage = `Invalid Role Code for the selected role. (Hint: Code must match role assignment)`;
      this.snackBar.open(this.errorMessage, 'Close', { duration: 4000 });
      return;
    }

    this.loading = true;
    try {
      // Send registration payload (excluding the client-only roleCode)
      const { roleCode, ...payload } = this.user;
      await this.authService.registerUser(payload);
      this.snackBar.open('Registration successful! Please sign in.', 'Close', { duration: 4000 });
      this.router.navigate(['/login']);
    } catch (error: any) {
      this.errorMessage = error.message || 'Registration failed';
      this.snackBar.open(this.errorMessage, 'Close', { duration: 4000 });
    } finally {
      this.loading = false;
    }
  }
}
