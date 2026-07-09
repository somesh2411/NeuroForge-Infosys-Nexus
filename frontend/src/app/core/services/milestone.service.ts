import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Milestone {
  id: string;
  projectId: string;
  name: string;
  description: string;
  targetDate: string;
  status: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class MilestoneService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1/milestones';

  getMilestonesByProject(projectId: string): Observable<Milestone[]> {
    return this.http.get<Milestone[]>(`${this.baseUrl}/project/${projectId}`);
  }

  getMilestoneById(id: string): Observable<Milestone> {
    return this.http.get<Milestone>(`${this.baseUrl}/${id}`);
  }

  createMilestone(projectId: string, milestone: Partial<Milestone>): Observable<Milestone> {
    return this.http.post<Milestone>(`${this.baseUrl}/project/${projectId}`, milestone);
  }

  updateMilestone(id: string, milestone: Partial<Milestone>): Observable<Milestone> {
    return this.http.put<Milestone>(`${this.baseUrl}/${id}`, milestone);
  }

  deleteMilestone(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
