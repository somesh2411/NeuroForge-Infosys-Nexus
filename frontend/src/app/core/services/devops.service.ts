import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Environment {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  createdAt?: string;
  createdBy?: string;
}

export interface Pipeline {
  id: string;
  name: string;
  projectId: string;
  repositoryId?: string;
  repositoryName?: string;
  repositoryUrl?: string;
  branch: string;
  buildTool: string;
  pipelineType: string;
  pipelineTemplate?: string;
  jenkinsJobName?: string;
  githubWorkflowPath?: string;
  enabled: boolean;
  status: string;
  createdAt?: string;
}

export interface Build {
  id: string;
  pipelineId: string;
  pipelineName: string;
  buildNumber: number;
  commitId: string;
  branch: string;
  status: string;
  triggerType: string;
  triggeredBy: string;
  startTime?: string;
  endTime?: string;
  durationMs?: number;
  artifactName?: string;
  artifactSize?: number;
  testsTotal?: number;
  testsPassed?: number;
  testsFailed?: number;
  createdAt?: string;
}

export interface PipelineStage {
  id: string;
  buildId: string;
  name: string;
  status: string;
  startTime?: string;
  endTime?: string;
  durationMs?: number;
  stageLog?: string;
}

export interface Deployment {
  id: string;
  environmentId: string;
  environmentName: string;
  buildId: string;
  buildNumber: number;
  pipelineName: string;
  status: string;
  version: string;
  deployedBy: string;
  deployedAt: string;
  durationMs?: number;
  rollbackAvailable: boolean;
  rolledBackFromDeploymentId?: string;
  rollbackReason?: string;
  deploymentLog?: string;
}

export interface Release {
  id: string;
  version: string;
  name: string;
  releaseNotes: string;
  buildId: string;
  buildNumber: number;
  pipelineName: string;
  status: string;
  releasedBy?: string;
  releasedAt?: string;
  createdAt?: string;
}

export interface DevOpsMetrics {
  totalBuilds: number;
  successfulBuilds: number;
  failedBuilds: number;
  runningBuilds: number;
  buildSuccessRate: number;
  averageBuildDurationSec: number;
  totalDeployments: number;
  successfulDeployments: number;
  deploymentSuccessRate: number;
  totalReleases: number;
  buildTrends: Array<{ date: string; success: number; failed: number }>;
  envStatus: Array<{ envId: string; envName: string; enabled: boolean; activeVersion: string; lastDeployStatus: string }>;
}

export interface BuildCompare {
  buildA: Build;
  buildB: Build;
  statusMatch: boolean;
  durationDifferenceMs: number;
  commitMatch: boolean;
  testCountDifference: number;
  deploymentComparisonNote: string;
}

export interface AuditEvent {
  id: string;
  eventType: string;
  message: string;
  actor: string;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class DevOpsService {
  private http = inject(HttpClient);
  private gatewayUrl = 'http://localhost:8080/api/v1';

  // Environment Management Endpoints
  getEnvironments(): Observable<Environment[]> {
    return this.http.get<Environment[]>(`${this.gatewayUrl}/devops-environments`);
  }

  getEnvironmentById(id: string): Observable<Environment> {
    return this.http.get<Environment>(`${this.gatewayUrl}/devops-environments/${id}`);
  }

  createEnvironment(env: Partial<Environment>): Observable<Environment> {
    return this.http.post<Environment>(`${this.gatewayUrl}/devops-environments`, env);
  }

  updateEnvironment(id: string, env: Partial<Environment>): Observable<Environment> {
    return this.http.put<Environment>(`${this.gatewayUrl}/devops-environments/${id}`, env);
  }

  deleteEnvironment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.gatewayUrl}/devops-environments/${id}`);
  }

  toggleEnvironment(id: string, enabled: boolean): Observable<Environment> {
    return this.http.put<Environment>(`${this.gatewayUrl}/devops-environments/${id}/toggle?enabled=${enabled}`, {});
  }

  // Pipeline Configuration Endpoints
  getPipelinesByProject(projectId: string): Observable<Pipeline[]> {
    return this.http.get<Pipeline[]>(`${this.gatewayUrl}/pipelines/project/${projectId}`);
  }

  getPipelineById(id: string): Observable<Pipeline> {
    return this.http.get<Pipeline>(`${this.gatewayUrl}/pipelines/${id}`);
  }

  createPipeline(pipeline: Partial<Pipeline>): Observable<Pipeline> {
    return this.http.post<Pipeline>(`${this.gatewayUrl}/pipelines`, pipeline);
  }

  updatePipeline(id: string, pipeline: Partial<Pipeline>): Observable<Pipeline> {
    return this.http.put<Pipeline>(`${this.gatewayUrl}/pipelines/${id}`, pipeline);
  }

  deletePipeline(id: string): Observable<void> {
    return this.http.delete<void>(`${this.gatewayUrl}/pipelines/${id}`);
  }

  togglePipeline(id: string, enabled: boolean): Observable<Pipeline> {
    return this.http.put<Pipeline>(`${this.gatewayUrl}/pipelines/${id}/toggle?enabled=${enabled}`, {});
  }

  // Build execution endpoints
  getBuildsByPipeline(pipelineId: string): Observable<Build[]> {
    return this.http.get<Build[]>(`${this.gatewayUrl}/builds/pipeline/${pipelineId}`);
  }

  getBuildById(id: string): Observable<Build> {
    return this.http.get<Build>(`${this.gatewayUrl}/builds/${id}`);
  }

  triggerBuild(pipelineId: string, triggerType: string = 'MANUAL'): Observable<Build> {
    return this.http.post<Build>(`${this.gatewayUrl}/builds/pipeline/${pipelineId}/trigger?triggerType=${triggerType}`, {});
  }

  getBuildStages(buildId: string): Observable<PipelineStage[]> {
    return this.http.get<PipelineStage[]>(`${this.gatewayUrl}/builds/${buildId}/stages`);
  }

  compareBuilds(buildIdA: string, buildIdB: string): Observable<BuildCompare> {
    return this.http.get<BuildCompare>(`${this.gatewayUrl}/builds/compare?buildIdA=${buildIdA}&buildIdB=${buildIdB}`);
  }

  getRecentProjectBuilds(projectId: string): Observable<Build[]> {
    return this.http.get<Build[]>(`${this.gatewayUrl}/builds/project/${projectId}/recent`);
  }

  // Deployment Endpoints
  getDeploymentsByEnvironment(environmentId: string): Observable<Deployment[]> {
    return this.http.get<Deployment[]>(`${this.gatewayUrl}/deployments/environment/${environmentId}`);
  }

  getProjectDeployments(projectId: string): Observable<Deployment[]> {
    return this.http.get<Deployment[]>(`${this.gatewayUrl}/deployments/project/${projectId}`);
  }

  triggerDeployment(environmentId: string, buildId: string, version: string): Observable<Deployment> {
    return this.http.post<Deployment>(`${this.gatewayUrl}/deployments`, { environmentId, buildId, version });
  }

  rollbackDeployment(deploymentId: string, reason?: string): Observable<Deployment> {
    const url = reason ? `${this.gatewayUrl}/deployments/${deploymentId}/rollback?reason=${encodeURIComponent(reason)}` : `${this.gatewayUrl}/deployments/${deploymentId}/rollback`;
    return this.http.post<Deployment>(url, {});
  }

  getDeploymentById(id: string): Observable<Deployment> {
    return this.http.get<Deployment>(`${this.gatewayUrl}/deployments/${id}`);
  }

  // Release Catalog Endpoints
  getReleasesByProject(projectId: string): Observable<Release[]> {
    return this.http.get<Release[]>(`${this.gatewayUrl}/releases/project/${projectId}`);
  }

  createRelease(release: Partial<Release>): Observable<Release> {
    return this.http.post<Release>(`${this.gatewayUrl}/releases`, release);
  }

  updateRelease(id: string, release: Partial<Release>): Observable<Release> {
    return this.http.put<Release>(`${this.gatewayUrl}/releases/${id}`, release);
  }

  deleteRelease(id: string): Observable<void> {
    return this.http.delete<void>(`${this.gatewayUrl}/releases/${id}`);
  }

  // Analytics & Timeline Audit logs Endpoints
  getDevOpsMetrics(projectId: string): Observable<DevOpsMetrics> {
    return this.http.get<DevOpsMetrics>(`${this.gatewayUrl}/devops-analytics/project/${projectId}`);
  }

  getAuditTimeline(): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(`${this.gatewayUrl}/devops-analytics/audit-events`);
  }
}
