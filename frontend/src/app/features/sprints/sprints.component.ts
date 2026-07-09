import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { SprintService, Sprint, SprintMetrics, BurndownPoint, VelocityPoint, ActivityLog } from '../../core/services/sprint.service';
import { ProjectService, Project } from '../../core/services/project.service';
import { UserService, User } from '../../core/services/user.service';
import { TaskService, Task, Blocker, Comment } from '../../core/services/task.service';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { forkJoin } from 'rxjs';

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
    MatSelectModule,
    MatTabsModule,
    MatProgressBarModule,
    DragDropModule
  ],
  templateUrl: './sprints.component.html',
  styleUrls: ['./sprints.component.css']
})
export class SprintsComponent implements OnInit {
  private sprintService = inject(SprintService);
  private projectService = inject(ProjectService);
  private userService = inject(UserService);
  private taskService = inject(TaskService);
  authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private notification = inject(NotificationService);

  // Sprints Scheduling List State
  projects: Project[] = [];
  selectedProject?: Project;
  sprints: Sprint[] = [];
  sprintForm: FormGroup;
  showForm = false;
  isEdit = false;
  selectedSprint?: Sprint;
  loading = false;

  // Sprint Execution Workspace State
  selectedSprintWorkspace?: Sprint;
  activeWorkspaceTab = 0; // 0: Backlog, 1: Kanban Board, 2: Analytics & Timeline
  
  // Workspace Data Lists
  workspaceTasks: Task[] = [];
  projectBacklogTasks: Task[] = [];
  usersList: User[] = [];
  
  // Kanban Board Columns Maps
  todoTasks: Task[] = [];
  inProgressTasks: Task[] = [];
  testingTasks: Task[] = [];
  doneTasks: Task[] = [];

  // Analytics & Timeline Data
  sprintMetrics?: SprintMetrics;
  burndownPoints: BurndownPoint[] = [];
  velocityPoints: VelocityPoint[] = [];
  sprintActivities: ActivityLog[] = [];

  // SVG Chart Calculation Strings
  idealBurndownLine = '';
  actualBurndownLine = '';
  burndownGridLines: Array<{ y: number, label: string }> = [];
  burndownXLabels: Array<{ x: number, label: string }> = [];
  velocityBars: Array<{ x: number, y: number, height: number, label: string, points: number }> = [];

  // Modals state
  showTaskFormModal = false;
  isTaskEdit = false;
  taskForm: FormGroup;
  selectedTaskForEdit?: Task;
  showTaskDetailsModal = false;
  selectedTaskDetails?: Task;

  // Dialog inner forms
  newCommentContent = '';
  newBlockerName = '';
  taskComments: Comment[] = [];
  taskBlockers: Blocker[] = [];
  taskActivities: ActivityLog[] = [];

  // Task Search and Filtering parameters
  taskSearchQuery = '';
  taskAssigneeFilter = '';
  taskPriorityFilter = '';
  taskSortBy = 'createdAt';
  taskSortDir = 'DESC';

  constructor() {
    this.sprintForm = this.fb.group({
      name: ['', Validators.required],
      goal: [''],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      capacity: [20, [Validators.required, Validators.min(0)]],
      status: ['PLANNED']
    });

    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(250)]],
      description: [''],
      assignedDeveloperId: [''],
      priority: ['MEDIUM', Validators.required],
      status: ['TO_DO', Validators.required],
      storyPoints: [1, [Validators.required, Validators.min(1)]],
      dueDate: [''],
      labels: [''],
      estimatedHours: [0, [Validators.min(0)]],
      actualHours: [0, [Validators.min(0)]],
      sprintId: ['']
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
        this.selectedSprintWorkspace = undefined;
        this.loadSprints(found.id);
      }
    } else {
      this.selectedProject = proj;
      this.selectedSprintWorkspace = undefined;
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

  // ==========================================
  // SPRINT EXECUTION WORKSPACE (MILESTONE 2)
  // ==========================================
  
  openSprintWorkspace(sprint: Sprint): void {
    this.selectedSprintWorkspace = sprint;
    this.activeWorkspaceTab = 0; // Start at Backlog tab
    this.loadWorkspaceData();
  }

  closeWorkspace(): void {
    this.selectedSprintWorkspace = undefined;
    if (this.selectedProject) {
      this.loadSprints(this.selectedProject.id);
    }
  }

  loadWorkspaceData(): void {
    if (!this.selectedSprintWorkspace || !this.selectedProject) return;
    this.loading = true;

    forkJoin({
      sprintTasks: this.taskService.getTasksBySprint(this.selectedSprintWorkspace.id),
      backlogTasks: this.taskService.getBacklogTasks(this.selectedProject.id),
      users: this.userService.getAllUsers()
    }).subscribe({
      next: (res) => {
        this.workspaceTasks = res.sprintTasks;
        this.projectBacklogTasks = res.backlogTasks;
        this.usersList = res.users;
        this.sortAndFilterWorkspaceTasks();
        this.buildKanbanBoard();
        this.loadMetricsAndCharts();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading workspace data', err);
        this.notification.error('Failed to load agile workspace data.');
        this.loading = false;
      }
    });
  }

  loadMetricsAndCharts(): void {
    if (!this.selectedSprintWorkspace || !this.selectedProject) return;

    this.sprintService.getSprintMetrics(this.selectedSprintWorkspace.id).subscribe({
      next: (m) => this.sprintMetrics = m,
      error: (err) => console.error('Error fetching metrics', err)
    });

    this.sprintService.getBurndownData(this.selectedSprintWorkspace.id).subscribe({
      next: (pts) => {
        this.burndownPoints = pts;
        this.renderBurndownChart();
      },
      error: (err) => console.error('Error loading burndown points', err)
    });

    this.sprintService.getProjectVelocity(this.selectedProject.id).subscribe({
      next: (pts) => {
        this.velocityPoints = pts;
        this.renderVelocityChart();
      },
      error: (err) => console.error('Error loading velocity points', err)
    });

    this.sprintService.getSprintActivity(this.selectedSprintWorkspace.id).subscribe({
      next: (logs) => this.sprintActivities = logs,
      error: (err) => console.error('Error fetching sprint activities', err)
    });
  }

  sortAndFilterWorkspaceTasks(): void {
    // Client-side filtering/sorting for rapid UX
    // Sprint Tasks
    let filtered = [...this.workspaceTasks];
    if (this.taskSearchQuery) {
      const q = this.taskSearchQuery.toLowerCase();
      filtered = filtered.filter(t => t.title.toLowerCase().includes(q) || (t.description && t.description.toLowerCase().includes(q)));
    }
    if (this.taskAssigneeFilter) {
      filtered = filtered.filter(t => t.assignedDeveloperId === this.taskAssigneeFilter);
    }
    if (this.taskPriorityFilter) {
      filtered = filtered.filter(t => t.priority === this.taskPriorityFilter);
    }
    
    // Sort
    filtered.sort((a, b) => {
      const fieldA = (a as any)[this.taskSortBy];
      const fieldB = (b as any)[this.taskSortBy];
      if (!fieldA) return 1;
      if (!fieldB) return -1;
      
      const comp = fieldA.toString().localeCompare(fieldB.toString());
      return this.taskSortDir === 'ASC' ? comp : -comp;
    });

    this.workspaceTasks = filtered;
  }

  buildKanbanBoard(): void {
    this.todoTasks = this.workspaceTasks.filter(t => t.status === 'TO_DO');
    this.inProgressTasks = this.workspaceTasks.filter(t => t.status === 'IN_PROGRESS');
    this.testingTasks = this.workspaceTasks.filter(t => t.status === 'TESTING' || t.status === 'CODE_REVIEW');
    this.doneTasks = this.workspaceTasks.filter(t => t.status === 'DONE');
  }

  onTaskDropped(event: CdkDragDrop<Task[]>, newStatus: string): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      const task = event.previousContainer.data[event.previousIndex];
      const oldStatus = task.status;
      
      // Optimistic UI updates
      task.status = newStatus;
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );

      // Persist to backend database with version mapping
      this.taskService.updateTaskStatus(task.id, newStatus, task.version).subscribe({
        next: (updated) => {
          task.version = updated.version;
          this.loadMetricsAndCharts();
          this.notification.success(`Task status updated to ${newStatus}.`);
        },
        error: (err) => {
          console.error('Error moving task', err);
          this.notification.error('Concurrent modification error. Reloading status.');
          // Revert container movement on failure
          this.loadWorkspaceData();
        }
      });
    }
  }

  // ==========================================
  // SPRINT CONTROLS
  // ==========================================

  startSprintWorkspace(): void {
    if (!this.selectedSprintWorkspace) return;
    if (confirm('Start this sprint cycle now? This will set its status to ACTIVE.')) {
      this.loading = true;
      this.sprintService.startSprint(this.selectedSprintWorkspace.id).subscribe({
        next: (s) => {
          this.selectedSprintWorkspace = s;
          this.loadMetricsAndCharts();
          this.notification.success('Sprint started successfully!');
          this.loading = false;
        },
        error: (err) => {
          console.error('Error starting sprint', err);
          this.notification.error(err.error?.message || 'Failed to start sprint.');
          this.loading = false;
        }
      });
    }
  }

  completeSprintWorkspace(): void {
    if (!this.selectedSprintWorkspace) return;
    if (confirm('Complete this sprint? Unfinished tasks will roll back to the project backlog automatically.')) {
      this.loading = true;
      this.sprintService.completeSprint(this.selectedSprintWorkspace.id).subscribe({
        next: (s) => {
          this.selectedSprintWorkspace = s;
          this.loadWorkspaceData();
          this.notification.success('Sprint completed successfully!');
          this.loading = false;
        },
        error: (err) => {
          console.error('Error completing sprint', err);
          this.notification.error('Failed to complete sprint.');
          this.loading = false;
        }
      });
    }
  }

  // ==========================================
  // TASK CRUD MODALS
  // ==========================================

  openCreateTaskModal(backlogOnly = false): void {
    this.isTaskEdit = false;
    this.showTaskFormModal = true;
    this.taskForm.reset({
      priority: 'MEDIUM',
      status: 'TO_DO',
      storyPoints: 1,
      estimatedHours: 0,
      actualHours: 0,
      sprintId: backlogOnly ? '' : (this.selectedSprintWorkspace?.id || '')
    });
  }

  openEditTaskModal(task: Task): void {
    this.isTaskEdit = true;
    this.selectedTaskForEdit = task;
    this.showTaskFormModal = true;
    this.taskForm.patchValue({
      title: task.title,
      description: task.description,
      assignedDeveloperId: task.assignedDeveloperId,
      priority: task.priority,
      status: task.status,
      storyPoints: task.storyPoints,
      dueDate: task.dueDate ? task.dueDate.substring(0, 16) : '',
      labels: task.labels,
      estimatedHours: task.estimatedHours,
      actualHours: task.actualHours,
      sprintId: task.sprintId || ''
    });
  }

  closeTaskFormModal(): void {
    this.showTaskFormModal = false;
    this.selectedTaskForEdit = undefined;
  }

  saveTask(): void {
    if (this.taskForm.invalid || !this.selectedProject) return;
    const formVal = this.taskForm.value;
    const payload = {
      ...formVal,
      projectId: this.selectedProject.id,
      dueDate: formVal.dueDate ? this.formatDateTimeToISO(formVal.dueDate) : null
    };

    this.loading = true;
    if (this.isTaskEdit && this.selectedTaskForEdit) {
      this.taskService.updateTask(this.selectedTaskForEdit.id, payload).subscribe({
        next: () => {
          this.notification.success('Task updated successfully.');
          this.closeTaskFormModal();
          this.loadWorkspaceData();
        },
        error: (err) => {
          console.error('Error updating task', err);
          this.notification.error('Failed to update task details.');
          this.loading = false;
        }
      });
    } else {
      this.taskService.createTask(payload).subscribe({
        next: () => {
          this.notification.success('Task created successfully.');
          this.closeTaskFormModal();
          this.loadWorkspaceData();
        },
        error: (err) => {
          console.error('Error creating task', err);
          this.notification.error('Failed to create task.');
          this.loading = false;
        }
      });
    }
  }

  deleteTask(id: string): void {
    if (confirm('Are you sure you want to soft-delete this task?')) {
      this.loading = true;
      this.taskService.deleteTask(id).subscribe({
        next: () => {
          this.notification.success('Task soft-deleted.');
          if (this.showTaskDetailsModal) this.closeTaskDetails();
          this.loadWorkspaceData();
        },
        error: (err) => {
          console.error('Error deleting task', err);
          this.notification.error('Failed to delete task.');
          this.loading = false;
        }
      });
    }
  }

  // ==========================================
  // TASK DETAILS OVERLAY / MODAL
  // ==========================================

  openTaskDetails(task: Task): void {
    this.selectedTaskDetails = task;
    this.showTaskDetailsModal = true;
    this.newCommentContent = '';
    this.newBlockerName = '';
    this.loadTaskRelations();
  }

  closeTaskDetails(): void {
    this.showTaskDetailsModal = false;
    this.selectedTaskDetails = undefined;
  }

  loadTaskRelations(): void {
    if (!this.selectedTaskDetails) return;
    const taskId = this.selectedTaskDetails.id;

    forkJoin({
      comments: this.taskService.getCommentsByTask(taskId),
      blockers: this.taskService.getBlockersByTask(taskId),
      activities: this.taskService.getTaskActivity(taskId)
    }).subscribe({
      next: (res) => {
        this.taskComments = res.comments;
        this.taskBlockers = res.blockers;
        this.taskActivities = res.activities;
      },
      error: (err) => console.error('Error loading task relations', err)
    });
  }

  postComment(): void {
    if (!this.selectedTaskDetails || !this.newCommentContent.trim()) return;
    this.taskService.addComment(this.selectedTaskDetails.id, this.newCommentContent).subscribe({
      next: (comment) => {
        this.taskComments.push(comment);
        this.newCommentContent = '';
        this.loadTaskRelations();
        this.notification.success('Comment posted.');
      },
      error: (err) => this.notification.error('Failed to post comment.')
    });
  }

  addBlocker(): void {
    if (!this.selectedTaskDetails || !this.newBlockerName.trim()) return;
    this.taskService.addBlocker(this.selectedTaskDetails.id, this.newBlockerName).subscribe({
      next: (blocker) => {
        this.taskBlockers.push(blocker);
        this.newBlockerName = '';
        this.loadTaskRelations();
        this.notification.success('Blocker logged.');
      },
      error: (err) => this.notification.error('Failed to log blocker.')
    });
  }

  resolveBlocker(blockerId: string): void {
    this.taskService.resolveBlocker(blockerId).subscribe({
      next: () => {
        this.loadTaskRelations();
        this.notification.success('Blocker resolved.');
      },
      error: (err) => this.notification.error('Failed to resolve blocker.')
    });
  }

  // ==========================================
  // DYNAMIC CHART RENDERING (PURE SVG LAYOUTS)
  // ==========================================

  renderBurndownChart(): void {
    if (this.burndownPoints.length === 0) {
      this.idealBurndownLine = '';
      this.actualBurndownLine = '';
      return;
    }

    const width = 600;
    const height = 300;
    const padding = 40;
    const chartWidth = width - 2 * padding;
    const chartHeight = height - 2 * padding;

    // Determine scale bounds
    const maxPoints = Math.max(
      ...this.burndownPoints.map(p => p.idealRemaining),
      ...this.burndownPoints.map(p => p.actualRemaining).filter(v => v >= 0),
      10 // fallback min y-axis max
    );

    const totalDays = this.burndownPoints.length;

    // Grid lines calculation
    this.burndownGridLines = [];
    const step = 4;
    for (let k = 0; k <= step; k++) {
      const val = Math.round(maxPoints * k / step);
      const y = height - padding - (val * chartHeight / maxPoints);
      this.burndownGridLines.push({ y, label: val.toString() });
    }

    // X Labels calculation
    this.burndownXLabels = [];
    const labelStep = Math.max(1, Math.floor(totalDays / 6));
    for (let i = 0; i < totalDays; i += labelStep) {
      const x = padding + (i * chartWidth / (totalDays - 1 || 1));
      this.burndownXLabels.push({ x, label: this.burndownPoints[i].date });
    }

    // Build SVG Ideal Path
    const idealPoints: string[] = [];
    for (let i = 0; i < totalDays; i++) {
      const x = padding + (i * chartWidth / (totalDays - 1 || 1));
      const y = height - padding - (this.burndownPoints[i].idealRemaining * chartHeight / maxPoints);
      idealPoints.push(`${x},${y}`);
    }
    this.idealBurndownLine = idealPoints.join(' ');

    // Build SVG Actual Path
    const actualPoints: string[] = [];
    for (let i = 0; i < totalDays; i++) {
      const val = this.burndownPoints[i].actualRemaining;
      if (val < 0) break; // Future day, stop line
      const x = padding + (i * chartWidth / (totalDays - 1 || 1));
      const y = height - padding - (val * chartHeight / maxPoints);
      actualPoints.push(`${x},${y}`);
    }
    this.actualBurndownLine = actualPoints.join(' ');
  }

  renderVelocityChart(): void {
    if (this.velocityPoints.length === 0) {
      this.velocityBars = [];
      return;
    }

    const width = 600;
    const height = 300;
    const padding = 45;
    const chartWidth = width - 2 * padding;
    const chartHeight = height - 2 * padding;

    const maxPoints = Math.max(
      ...this.velocityPoints.map(p => p.completedStoryPoints),
      10 // fallback
    );

    const totalBars = this.velocityPoints.length;
    const barWidth = Math.max(15, (chartWidth / totalBars) * 0.6);
    const spacing = (chartWidth - (totalBars * barWidth)) / (totalBars + 1 || 1);

    this.velocityBars = [];
    for (let i = 0; i < totalBars; i++) {
      const points = this.velocityPoints[i].completedStoryPoints;
      const barHeight = points * chartHeight / maxPoints;
      const x = padding + spacing + i * (barWidth + spacing);
      const y = height - padding - barHeight;

      this.velocityBars.push({
        x,
        y,
        height: barHeight,
        label: this.velocityPoints[i].sprintName,
        points
      });
    }
  }

  // Backlog-to-Sprint fast drag helper (for Backlog panel list)
  moveTaskToSprint(task: Task, sprintId: string | null): void {
    this.loading = true;
    this.taskService.updateTask(task.id, {
      projectId: task.projectId,
      title: task.title,
      sprintId: sprintId || ''
    }).subscribe({
      next: () => {
        this.notification.success(sprintId ? 'Task added to sprint.' : 'Task rolled back to backlog.');
        this.loadWorkspaceData();
      },
      error: () => {
        this.notification.error('Failed to move task.');
        this.loading = false;
      }
    });
  }
}
