import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Project {
  id: string;
  name: string;
  key: string;
  description: string;
  managerId: string;
  managerName?: string;
  status: string;
  teamIds: string[];
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1/projects';

  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.baseUrl);
  }

  getProjectById(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.baseUrl}/${id}`);
  }

  getProjectByKey(key: string): Observable<Project> {
    return this.http.get<Project>(`${this.baseUrl}/key/${key}`);
  }

  createProject(project: Partial<Project>): Observable<Project> {
    return this.http.post<Project>(this.baseUrl, project);
  }

  updateProject(id: string, project: Partial<Project>): Observable<Project> {
    return this.http.put<Project>(`${this.baseUrl}/${id}`, project);
  }

  deleteProject(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  associateTeam(projectId: string, teamId: string): Observable<Project> {
    return this.http.post<Project>(`${this.baseUrl}/${projectId}/teams?teamId=${teamId}`, {});
  }

  disassociateTeam(projectId: string, teamId: string): Observable<Project> {
    return this.http.delete<Project>(`${this.baseUrl}/${projectId}/teams/${teamId}`);
  }
}
