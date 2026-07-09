import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ActivityLog } from './sprint.service';

export interface Task {
  id: string;
  projectId: string;
  sprintId?: string;
  sprintName?: string;
  title: string;
  description: string;
  assignedDeveloperId?: string;
  assignedDeveloperName: string;
  priority: string; // LOW, MEDIUM, HIGH, CRITICAL
  status: string; // TO_DO, IN_PROGRESS, CODE_REVIEW, TESTING, DONE
  storyPoints: number;
  dueDate?: string;
  labels?: string;
  estimatedHours: number;
  actualHours: number;
  version: number;
  createdAt: string;
  createdBy: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface Blocker {
  id: string;
  taskId: string;
  name: string;
  status: string; // ACTIVE, RESOLVED
  resolvedAt?: string;
  resolvedBy?: string;
  createdAt: string;
  createdBy: string;
}

export interface Comment {
  id: string;
  taskId: string;
  content: string;
  authorUsername: string;
  createdAt: string;
}

export interface PaginatedTasks {
  content: Task[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private http = inject(HttpClient);
  private tasksUrl = 'http://localhost:8080/api/v1/tasks';
  private blockersUrl = 'http://localhost:8080/api/v1/blockers';
  private commentsUrl = 'http://localhost:8080/api/v1/comments';
  private analyticsUrl = 'http://localhost:8080/api/v1/analytics';

  getTasks(filters: {
    projectId?: string;
    sprintId?: string;
    developerId?: string;
    priority?: string;
    status?: string;
    search?: string;
    sortBy?: string;
    sortDir?: string;
    page?: number;
    size?: number;
  }): Observable<PaginatedTasks> {
    let params = new HttpParams();
    if (filters.projectId) params = params.set('projectId', filters.projectId);
    if (filters.sprintId) params = params.set('sprintId', filters.sprintId);
    if (filters.developerId) params = params.set('developerId', filters.developerId);
    if (filters.priority) params = params.set('priority', filters.priority);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.search) params = params.set('search', filters.search);
    if (filters.sortBy) params = params.set('sortBy', filters.sortBy);
    if (filters.sortDir) params = params.set('sortDir', filters.sortDir);
    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());

    return this.http.get<PaginatedTasks>(this.tasksUrl, { params });
  }

  getTaskById(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.tasksUrl}/${id}`);
  }

  getTasksBySprint(sprintId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.tasksUrl}/sprint/${sprintId}`);
  }

  getBacklogTasks(projectId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.tasksUrl}/project/${projectId}/backlog`);
  }

  createTask(task: Partial<Task>): Observable<Task> {
    return this.http.post<Task>(this.tasksUrl, task);
  }

  updateTask(id: string, task: Partial<Task>): Observable<Task> {
    return this.http.put<Task>(`${this.tasksUrl}/${id}`, task);
  }

  updateTaskStatus(id: string, status: string, version: number): Observable<Task> {
    let params = new HttpParams()
      .set('status', status)
      .set('version', version.toString());
    return this.http.put<Task>(`${this.tasksUrl}/${id}/status`, {}, { params });
  }

  deleteTask(id: string): Observable<void> {
    return this.http.delete<void>(`${this.tasksUrl}/${id}`);
  }

  // Blocker Management
  addBlocker(taskId: string, name: string): Observable<Blocker> {
    return this.http.post<Blocker>(`${this.blockersUrl}/task/${taskId}`, { name });
  }

  resolveBlocker(blockerId: string): Observable<Blocker> {
    return this.http.put<Blocker>(`${this.blockersUrl}/${blockerId}/resolve`, {});
  }

  deleteBlocker(blockerId: string): Observable<void> {
    return this.http.delete<void>(`${this.blockersUrl}/${blockerId}`);
  }

  getBlockersByTask(taskId: string): Observable<Blocker[]> {
    return this.http.get<Blocker[]>(`${this.blockersUrl}/task/${taskId}`);
  }

  // Comment Management
  addComment(taskId: string, content: string): Observable<Comment> {
    return this.http.post<Comment>(`${this.commentsUrl}/task/${taskId}`, { content });
  }

  deleteComment(commentId: string): Observable<void> {
    return this.http.delete<void>(`${this.commentsUrl}/${commentId}`);
  }

  getCommentsByTask(taskId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.commentsUrl}/task/${taskId}`);
  }

  // Task Activity Log History
  getTaskActivity(taskId: string): Observable<ActivityLog[]> {
    return this.http.get<ActivityLog[]>(`${this.analyticsUrl}/task/${taskId}/activity`);
  }
}
