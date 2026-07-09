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

@Injectable({
  providedIn: 'root'
})
export class SprintService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1/sprints';

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
}
