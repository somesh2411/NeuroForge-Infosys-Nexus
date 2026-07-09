import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { SprintService, Sprint } from '../../core/services/sprint.service';
import { ProjectService, Project } from '../../core/services/project.service';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-sprints',
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
  templateUrl: './sprints.component.html',
  styleUrls: ['./sprints.component.css']
})
export class SprintsComponent implements OnInit {
  private sprintService = inject(SprintService);
  private projectService = inject(ProjectService);
  authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private notification = inject(NotificationService);

  projects: Project[] = [];
  selectedProject?: Project;
  sprints: Sprint[] = [];
  
  sprintForm: FormGroup;
  showForm = false;
  isEdit = false;
  selectedSprint?: Sprint;
  loading = false;

  constructor() {
    this.sprintForm = this.fb.group({
      name: ['', Validators.required],
      goal: [''],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      capacity: [20, [Validators.required, Validators.min(0)]],
      status: ['PLANNED']
    });
  }

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading = true;
    this.projectService.getAllProjects().subscribe({
      next: (data) => {
        this.projects = data;
        if (data.length > 0) {
          this.selectProject(data[0]);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching projects', err);
        this.loading = false;
      }
    });
  }

  selectProject(proj: Project | string): void {
    if (typeof proj === 'string') {
      const found = this.projects.find(p => p.id === proj);
      if (found) {
        this.selectedProject = found;
        this.loadSprints(found.id);
      }
    } else {
      this.selectedProject = proj;
      this.loadSprints(proj.id);
    }
  }

  loadSprints(projectId: string): void {
    this.sprintService.getSprintsByProject(projectId).subscribe({
      next: (data) => this.sprints = data,
      error: (err) => console.error('Error fetching sprints', err)
    });
  }

  toggleSprintForm(edit = false, sprint?: Sprint): void {
    this.showForm = !this.showForm;
    this.isEdit = edit;
    if (!this.showForm) {
      this.sprintForm.reset({ capacity: 20, status: 'PLANNED' });
      this.selectedSprint = undefined;
    } else if (edit && sprint) {
      this.selectedSprint = sprint;
      this.sprintForm.patchValue({
        name: sprint.name,
        goal: sprint.goal,
        startDate: sprint.startDate ? sprint.startDate.substring(0, 16) : '',
        endDate: sprint.endDate ? sprint.endDate.substring(0, 16) : '',
        capacity: sprint.capacity,
        status: sprint.status
      });
    }
  }

  formatDateTimeToISO(dtStr: string): string {
    if (!dtStr) return '';
    if (dtStr.includes('T')) return dtStr;
    
    const dmyRegex = /^(\d{2})-(\d{2})-(\d{4})\s+(\d{2}):(\d{2})(?::(\d{2}))?$/;
    const match = dtStr.match(dmyRegex);
    if (match) {
      const [_, day, month, year, hour, minute, second] = match;
      return `${year}-${month}-${day}T${hour}:${minute}:${second || '00'}`;
    }
    
    try {
      const d = new Date(dtStr);
      if (!isNaN(d.getTime())) {
        const pad = (n: number) => n.toString().padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
      }
    } catch (e) {
      console.error("Failed to parse date string", dtStr, e);
    }
    return dtStr;
  }

  saveSprint(): void {
    if (this.sprintForm.invalid || !this.selectedProject) return;
    const formVal = this.sprintForm.value;
    const payload = {
      ...formVal,
      startDate: this.formatDateTimeToISO(formVal.startDate),
      endDate: this.formatDateTimeToISO(formVal.endDate)
    };

    this.loading = true;
    if (this.isEdit && this.selectedSprint) {
      this.sprintService.updateSprint(this.selectedSprint.id, payload).subscribe({
        next: () => {
          this.loadSprints(this.selectedProject!.id);
          this.toggleSprintForm();
          this.notification.success('Sprint settings updated successfully.');
          this.loading = false;
        },
        error: (err) => {
          console.error('Error updating sprint', err);
          this.notification.error(err.error?.message || 'Failed to update sprint settings.');
          this.loading = false;
        }
      });
    } else {
      this.sprintService.createSprint(this.selectedProject.id, payload).subscribe({
        next: () => {
          this.loadSprints(this.selectedProject!.id);
          this.toggleSprintForm();
          this.notification.success('Sprint scheduled successfully.');
          this.loading = false;
        },
        error: (err) => {
          console.error('Error creating sprint', err);
          this.notification.error(err.error?.message || 'Failed to schedule sprint.');
          this.loading = false;
        }
      });
    }
  }

  deleteSprint(id: string): void {
    if (confirm('Are you sure you want to soft-delete this sprint?')) {
      this.loading = true;
      this.sprintService.deleteSprint(id).subscribe({
        next: () => {
          if (this.selectedProject) {
            this.loadSprints(this.selectedProject.id);
          }
          this.notification.success('Sprint soft-deleted successfully.');
          this.loading = false;
        },
        error: (err) => {
          console.error('Error deleting sprint', err);
          this.notification.error(err.error?.message || 'Failed to delete sprint.');
          this.loading = false;
        }
      });
    }
  }
}
