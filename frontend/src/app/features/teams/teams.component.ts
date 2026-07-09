import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { UserService, Team, User } from '../../core/services/user.service';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-teams',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    ReactiveFormsModule, 
    MatCardModule, 
    MatIconModule, 
    MatButtonModule, 
    MatFormFieldModule, 
    MatInputModule, 
    MatSelectModule
  ],
  templateUrl: './teams.component.html',
  styleUrls: ['./teams.component.css']
})
export class TeamsComponent implements OnInit {
  private userService = inject(UserService);
  authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private notification = inject(NotificationService);

  teams: Team[] = [];
  users: User[] = [];
  selectedTeam?: Team;
  teamForm: FormGroup;
  showForm = false;
  isEdit = false;
  loading = false;

  constructor() {
    this.teamForm = this.fb.group({
      name: ['', Validators.required],
      code: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9_-]{3,10}$/)]],
      description: [''],
      leadId: ['']
    });
  }

  ngOnInit(): void {
    this.loadTeamsAndUsers();
  }

  loadTeamsAndUsers(): void {
    this.loading = true;
    this.userService.getAllTeams().subscribe({
      next: (teamsData) => {
        this.teams = teamsData;
        if (teamsData.length > 0 && !this.selectedTeam) {
          this.selectTeam(teamsData[0]);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching teams', err);
        this.loading = false;
      }
    });

    this.userService.getAllUsers().subscribe({
      next: (usersData) => {
        this.users = usersData;
      }
    });
  }

  selectTeam(team: Team): void {
    this.selectedTeam = team;
  }

  toggleForm(edit = false): void {
    this.showForm = !this.showForm;
    this.isEdit = edit;
    if (!this.showForm) {
      this.teamForm.reset();
    } else if (edit && this.selectedTeam) {
      this.teamForm.patchValue({
        name: this.selectedTeam.name,
        code: this.selectedTeam.code,
        description: this.selectedTeam.description,
        leadId: this.selectedTeam.leadId || ''
      });
    }
  }

  saveTeam(): void {
    if (this.teamForm.invalid) return;

    const payload = this.teamForm.value;
    payload.code = payload.code.toUpperCase(); // Normalize code to uppercase

    this.loading = true;
    if (this.isEdit && this.selectedTeam) {
      this.userService.updateTeam(this.selectedTeam.id, payload).subscribe({
        next: (updated) => {
          this.loadTeamsAndUsers();
          this.selectedTeam = updated;
          this.toggleForm();
          this.notification.success('Team configuration updated successfully.');
          this.loading = false;
        },
        error: (err) => {
          console.error('Error updating team', err);
          this.notification.error(err.error?.message || 'Failed to update team settings.');
          this.loading = false;
        }
      });
    } else {
      this.userService.createTeam(payload).subscribe({
        next: (created) => {
          this.loadTeamsAndUsers();
          this.selectedTeam = created;
          this.toggleForm();
          this.notification.success(`Team "${created.name}" created successfully.`);
          this.loading = false;
        },
        error: (err) => {
          console.error('Error creating team', err);
          this.notification.error(err.error?.message || 'Failed to create team.');
          this.loading = false;
        }
      });
    }
  }

  deleteTeam(id: string): void {
    if (confirm('Are you sure you want to delete this team?')) {
      this.loading = true;
      this.userService.deleteTeam(id).subscribe({
        next: () => {
          this.selectedTeam = undefined;
          this.loadTeamsAndUsers();
          this.notification.success('Team soft-deleted successfully.');
          this.loading = false;
        },
        error: (err) => {
          console.error('Error deleting team', err);
          this.notification.error(err.error?.message || 'Failed to delete team.');
          this.loading = false;
        }
      });
    }
  }

  addUserToTeam(userId: string): void {
    if (!this.selectedTeam || !userId) return;
    this.userService.addTeamMember(this.selectedTeam.id, userId).subscribe({
      next: (updated) => {
        this.selectedTeam = updated;
        this.loadTeamsAndUsers();
        this.notification.success('Team member assigned successfully.');
      },
      error: (err) => {
        console.error('Error adding team member', err);
        this.notification.error(err.error?.message || 'Failed to assign team member.');
      }
    });
  }

  removeUserFromTeam(userId: string): void {
    if (!this.selectedTeam) return;
    this.userService.removeTeamMember(this.selectedTeam.id, userId).subscribe({
      next: (updated) => {
        this.selectedTeam = updated;
        this.loadTeamsAndUsers();
        this.notification.success('Team member removed successfully.');
      },
      error: (err) => {
        console.error('Error removing team member', err);
        this.notification.error(err.error?.message || 'Failed to remove team member.');
      }
    });
  }

  getFilteredUsers(): User[] {
    if (!this.selectedTeam) return this.users;
    return this.users.filter(u => !this.selectedTeam?.memberIds.includes(u.id));
  }

  getUserNameById(id: string): string {
    const user = this.users.find(u => u.id === id);
    if (!user) return id;
    return user.firstName && user.lastName ? `${user.firstName} ${user.lastName}` : user.username;
  }
}
