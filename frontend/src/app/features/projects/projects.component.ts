import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ProjectService, Project } from '../../core/services/project.service';
import { UserService, Team, User } from '../../core/services/user.service';
import { MilestoneService, Milestone } from '../../core/services/milestone.service';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-projects',
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
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.css']
})
export class ProjectsComponent implements OnInit {
  private projectService = inject(ProjectService);
  private userService = inject(UserService);
  private milestoneService = inject(MilestoneService);
  authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private notification = inject(NotificationService);

  projects: Project[] = [];
  teams: Team[] = [];
  users: User[] = [];
  selectedProject?: Project;
  milestones: Milestone[] = [];
  
  projectForm: FormGroup;
  milestoneForm: FormGroup;
  
  showForm = false;
  showMilestoneForm = false;
  isEdit = false;
  loading = false;

  constructor() {
    this.projectForm = this.fb.group({
      name: ['', Validators.required],
      key: ['', [Validators.required, Validators.pattern(/^[a-zA-Z]{2,10}$/)]],
      description: [''],
      managerId: ['', Validators.required],
      status: ['ACTIVE']
    });

    this.milestoneForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      targetDate: ['', Validators.required],
      status: ['PLANNED']
    });
  }

  ngOnInit(): void {
    this.loadAllData();
  }

  loadAllData(): void {
    this.loading = true;
    this.projectService.getAllProjects().subscribe({
      next: (data) => {
        this.projects = data;
        if (data.length > 0 && !this.selectedProject) {
          this.selectProject(data[0]);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching projects', err);
        this.notification.error('Failed to load project database.');
        this.loading = false;
      }
    });

    this.userService.getAllTeams().subscribe({
      next: (t) => this.teams = t,
      error: () => this.notification.error('Failed to retrieve teams list.')
    });
    this.userService.getAllUsers().subscribe({
      next: (u) => this.users = u,
      error: () => this.notification.error('Failed to retrieve users list.')
    });
  }

  selectProject(proj: Project): void {
    this.selectedProject = proj;
    this.loadMilestones(proj.id);
  }

  loadMilestones(projectId: string): void {
    this.milestoneService.getMilestonesByProject(projectId).subscribe({
      next: (m) => this.milestones = m,
      error: (err) => console.error('Error fetching milestones', err)
    });
  }

  toggleProjectForm(edit = false): void {
    this.showForm = !this.showForm;
    this.isEdit = edit;
    if (!this.showForm) {
      this.projectForm.reset({ status: 'ACTIVE' });
    } else if (edit && this.selectedProject) {
      this.projectForm.patchValue({
        name: this.selectedProject.name,
        key: this.selectedProject.key,
        description: this.selectedProject.description,
        managerId: this.selectedProject.managerId,
        status: this.selectedProject.status
      });
    }
  }

  saveProject(): void {
    if (this.projectForm.invalid) return;
    const payload = this.projectForm.value;
    payload.key = payload.key.toUpperCase(); // Normalize key to uppercase

    this.loading = true;
    if (this.isEdit && this.selectedProject) {
      this.projectService.updateProject(this.selectedProject.id, payload).subscribe({
        next: (updated) => {
          this.loadAllData();
          this.selectedProject = updated;
          this.toggleProjectForm();
          this.notification.success('Project configuration updated successfully.');
          this.loading = false;
        },
        error: (err) => {
          console.error('Error updating project', err);
          this.notification.error(err.error?.message || 'Failed to update project settings.');
          this.loading = false;
        }
      });
    } else {
      this.projectService.createProject(payload).subscribe({
        next: (created) => {
          this.loadAllData();
          this.selectedProject = created;
          this.toggleProjectForm();
          this.notification.success(`Project "${created.name}" initiated successfully.`);
          this.loading = false;
        },
        error: (err) => {
          console.error('Error creating project', err);
          this.notification.error(err.error?.message || 'Failed to initiate project.');
          this.loading = false;
        }
      });
    }
  }

  deleteProject(id: string): void {
    if (confirm('Are you sure you want to soft-delete this project?')) {
      this.loading = true;
      this.projectService.deleteProject(id).subscribe({
        next: () => {
          this.selectedProject = undefined;
          this.loadAllData();
          this.notification.success('Project soft-deleted successfully.');
          this.loading = false;
        },
        error: (err) => {
          console.error('Error deleting project', err);
          this.notification.error(err.error?.message || 'Failed to delete project.');
          this.loading = false;
        }
      });
    }
  }

  // Milestone actions
  toggleMilestoneForm(): void {
    this.showMilestoneForm = !this.showMilestoneForm;
    if (!this.showMilestoneForm) {
      this.milestoneForm.reset({ status: 'PLANNED' });
    }
  }

  saveMilestone(): void {
    if (this.milestoneForm.invalid || !this.selectedProject) return;
    const payload = this.milestoneForm.value;
    
    this.milestoneService.createMilestone(this.selectedProject.id, payload).subscribe({
      next: () => {
        this.loadMilestones(this.selectedProject!.id);
        this.toggleMilestoneForm();
        this.notification.success('Milestone created successfully.');
      },
      error: (err) => {
        console.error('Error creating milestone', err);
        this.notification.error(err.error?.message || 'Failed to create milestone.');
      }
    });
  }

  deleteMilestone(id: string): void {
    if (confirm('Delete this milestone?')) {
      this.milestoneService.deleteMilestone(id).subscribe({
        next: () => {
          if (this.selectedProject) {
            this.loadMilestones(this.selectedProject.id);
          }
          this.notification.success('Milestone deleted successfully.');
        },
        error: (err) => {
          console.error('Error deleting milestone', err);
          this.notification.error(err.error?.message || 'Failed to delete milestone.');
        }
      });
    }
  }

  // Team association actions
  associateTeam(teamId: string): void {
    if (!this.selectedProject || !teamId) return;
    this.projectService.associateTeam(this.selectedProject.id, teamId).subscribe({
      next: (updated) => {
        this.selectedProject = updated;
        this.loadAllData();
        this.notification.success('Team aligned with project successfully.');
      },
      error: (err) => {
        console.error('Error associating team', err);
        this.notification.error(err.error?.message || 'Failed to align team.');
      }
    });
  }

  disassociateTeam(teamId: string): void {
    if (!this.selectedProject) return;
    this.projectService.disassociateTeam(this.selectedProject.id, teamId).subscribe({
      next: (updated) => {
        this.selectedProject = updated;
        this.loadAllData();
        this.notification.success('Team alignment removed successfully.');
      },
      error: (err) => {
        console.error('Error disassociating team', err);
        this.notification.error(err.error?.message || 'Failed to remove team alignment.');
      }
    });
  }

  getFilteredTeams(): Team[] {
    if (!this.selectedProject) return this.teams;
    return this.teams.filter(t => !this.selectedProject?.teamIds.includes(t.id));
  }

  getTeamNameById(id: string): string {
    const team = this.teams.find(t => t.id === id);
    return team ? `${team.name} (${team.code})` : id;
  }
}
