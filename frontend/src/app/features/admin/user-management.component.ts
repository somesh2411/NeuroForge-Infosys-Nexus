import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService, User, Team } from '../../core/services/user.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule
  ],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  private userService = inject(UserService);
  private snackBar = inject(MatSnackBar);

  users: User[] = [];
  filteredUsers: User[] = [];
  teams: Team[] = [];
  loading = true;
  searchQuery = '';

  // Edit Modal State
  showEditModal = false;
  editingUser?: User;
  editForm = {
    email: '',
    firstName: '',
    lastName: '',
    primaryTeamId: '',
    role: ''
  };

  roles = [
    { value: 'ADMIN', label: 'Admin' },
    { value: 'ORGANIZATION_OWNER', label: 'Organization Owner' },
    { value: 'TEAM_LEAD', label: 'Team Lead' },
    { value: 'DEVELOPER', label: 'Developer' },
    { value: 'QA', label: 'Quality Assurance' },
    { value: 'STAKEHOLDER', label: 'Stakeholder' }
  ];

  displayedColumns: string[] = ['username', 'email', 'name', 'team', 'role', 'actions'];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    forkJoin({
      users: this.userService.getAllUsers(),
      teams: this.userService.getAllTeams()
    }).subscribe({
      next: (res) => {
        this.users = res.users;
        this.filteredUsers = res.users;
        this.teams = res.teams;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading admin users data', err);
        this.snackBar.open('Failed to load users data', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) {
      this.filteredUsers = this.users;
      return;
    }
    this.filteredUsers = this.users.filter(u => 
      u.username.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      u.firstName.toLowerCase().includes(q) ||
      u.lastName.toLowerCase().includes(q)
    );
  }

  openEdit(user: User): void {
    this.editingUser = user;
    this.editForm = {
      email: user.email,
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      primaryTeamId: user.primaryTeamId || '',
      role: user.role || 'DEVELOPER'
    };
    this.showEditModal = true;
  }

  closeEdit(): void {
    this.showEditModal = false;
    this.editingUser = undefined;
  }

  saveEdit(): void {
    if (!this.editingUser) return;
    
    this.loading = true;
    this.userService.updateUser(this.editingUser.id, this.editForm).subscribe({
      next: (updated) => {
        this.snackBar.open('User updated successfully!', 'Close', { duration: 3000 });
        this.closeEdit();
        this.loadData();
      },
      error: (err) => {
        console.error('Failed to update user', err);
        this.snackBar.open('Error updating user', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  deleteUser(user: User): void {
    if (!confirm(`Are you sure you want to delete / archive user ${user.username}?`)) {
      return;
    }

    this.loading = true;
    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.snackBar.open('User deleted successfully!', 'Close', { duration: 3000 });
        this.loadData();
      },
      error: (err) => {
        console.error('Failed to delete user', err);
        this.snackBar.open('Error deleting user', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }
}
