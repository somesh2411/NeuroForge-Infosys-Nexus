import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Sprint {
  id: string;
  projectId: string;
  projectName?: string;
  name: string;
  goal: string;
  startDate: string;
  endDate: string;
  capacity: number;
  status: string;
  createdAt: string;
}

export interface SprintMetrics {
  sprintId: string;
  sprintName: string;
  sprintGoal: string;
  status: string;
  totalTasks: number;
  completedTasks: number;
  remainingTasks: number;
  totalStoryPoints: number;
  completedStoryPoints: number;
  remainingStoryPoints: number;
  progressPercentage: number;
}

export interface BurndownPoint {
  date: string;
  idealRemaining: number;
  actualRemaining: number;
}

export interface VelocityPoint {
  sprintId: string;
  sprintName: string;
  completedStoryPoints: number;
}

export interface ActivityLog {
  id: string;
  taskId?: string;
  sprintId?: string;
  eventType: string;
  message: string;
  actor: string;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class SprintService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1/sprints';
  private analyticsUrl = 'http://localhost:8080/api/v1/analytics';

  getSprintsByProject(projectId: string): Observable<Sprint[]> {
    return this.http.get<Sprint[]>(`${this.baseUrl}/project/${projectId}`);
  }

  getAllSprints(): Observable<Sprint[]> {
    return this.http.get<Sprint[]>(this.baseUrl);
  }

  getSprintById(id: string): Observable<Sprint> {
    return this.http.get<Sprint>(`${this.baseUrl}/${id}`);
  }

  createSprint(projectId: string, sprint: Partial<Sprint>): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.baseUrl}/project/${projectId}`, sprint);
  }

  updateSprint(id: string, sprint: Partial<Sprint>): Observable<Sprint> {
    return this.http.put<Sprint>(`${this.baseUrl}/${id}`, sprint);
  }

  deleteSprint(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  startSprint(id: string): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.baseUrl}/${id}/start`, {});
  }

  completeSprint(id: string): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.baseUrl}/${id}/complete`, {});
  }

  getSprintMetrics(sprintId: string): Observable<SprintMetrics> {
    return this.http.get<SprintMetrics>(`${this.analyticsUrl}/sprint/${sprintId}/metrics`);
  }

  getBurndownData(sprintId: string): Observable<BurndownPoint[]> {
    return this.http.get<BurndownPoint[]>(`${this.analyticsUrl}/sprint/${sprintId}/burndown`);
  }

  getProjectVelocity(projectId: string): Observable<VelocityPoint[]> {
    return this.http.get<VelocityPoint[]>(`${this.analyticsUrl}/project/${projectId}/velocity`);
  }

  getSprintActivity(sprintId: string): Observable<ActivityLog[]> {
    return this.http.get<ActivityLog[]>(`${this.analyticsUrl}/sprint/${sprintId}/activity`);
  }
}
