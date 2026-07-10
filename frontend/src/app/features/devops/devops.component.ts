import { Component, OnInit, OnDestroy, inject } from '@angular/core';
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
import { 
  DevOpsService, 
  Pipeline, 
  Build, 
  PipelineStage, 
  Deployment, 
  Release, 
  DevOpsMetrics, 
  BuildCompare, 
  AuditEvent, 
  Environment 
} from '../../core/services/devops.service';
import { ProjectService, Project } from '../../core/services/project.service';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-devops',
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
    MatProgressBarModule
  ],
  templateUrl: './devops.component.html',
  styleUrls: ['./devops.component.css']
})
export class DevOpsComponent implements OnInit, OnDestroy {
  private devopsService = inject(DevOpsService);
  private projectService = inject(ProjectService);
  private fb = inject(FormBuilder);
  private notification = inject(NotificationService);
  authService = inject(AuthService);

  // Layout states
  activeTab = 0; // 0: Dashboard, 1: Pipelines, 2: Stage Visualizer, 3: Deployments, 4: Releases, 5: Comparators, 6: Audits
  loading = false;
  pollingInterval: any;

  // Project selector state
  projects: Project[] = [];
  selectedProject?: Project;

  // DevOps configurations data
  environments: Environment[] = [];
  pipelines: Pipeline[] = [];
  recentBuilds: Build[] = [];
  releases: Release[] = [];
  deployments: Deployment[] = [];
  metrics?: DevOpsMetrics;
  auditLogs: AuditEvent[] = [];

  // Active select/drilldowns
  selectedPipeline?: Pipeline;
  pipelineBuilds: Build[] = [];
  selectedBuildDetails?: Build;
  selectedBuildStages: PipelineStage[] = [];
  selectedStageLog = '';
  selectedStageName = '';

  // Forms group state
  pipelineForm: FormGroup;
  showPipelineModal = false;
  isPipelineEdit = false;
  editingPipelineId?: string;

  envForm: FormGroup;
  showEnvModal = false;
  isEnvEdit = false;
  editingEnvId?: string;

  deployForm: FormGroup;
  showDeployModal = false;
  deployableBuilds: Build[] = [];

  releaseForm: FormGroup;
  showReleaseModal = false;
  isReleaseEdit = false;
  editingReleaseId?: string;
  releaseBuilds: Build[] = [];

  // Build Compare state
  compareForm: FormGroup;
  compareResult?: BuildCompare;

  // Rollback state
  showRollbackModal = false;
  rollbackTargetDeploymentId?: string;
  rollbackReason = '';

  constructor() {
    this.pipelineForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      projectId: ['', Validators.required],
      repositoryUrl: ['', [Validators.required, Validators.pattern('https?://.+')]],
      repositoryName: [''],
      branch: ['main', Validators.required],
      buildTool: ['MAVEN', Validators.required],
      pipelineType: ['MOCK', Validators.required],
      pipelineTemplate: ['SPRING_BOOT'],
      jenkinsJobName: [''],
      githubWorkflowPath: [''],
      enabled: [true]
    });

    this.envForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: [''],
      enabled: [true]
    });

    this.deployForm = this.fb.group({
      environmentId: ['', Validators.required],
      buildId: ['', Validators.required],
      version: ['', [Validators.required, Validators.pattern('v\\d+\\.\\d+\\.\\d+.*')]]
    });

    this.releaseForm = this.fb.group({
      version: ['', [Validators.required, Validators.pattern('\\d+\\.\\d+\\.\\d+.*')]],
      name: ['', Validators.required],
      releaseNotes: [''],
      buildId: ['', Validators.required],
      status: ['DRAFT', Validators.required]
    });

    this.compareForm = this.fb.group({
      buildIdA: ['', Validators.required],
      buildIdB: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadProjects();
    this.loadEnvironments();
    this.loadAuditTimeline();
  }

  ngOnDestroy(): void {
    this.stopBuildPolling();
  }

  // Projects Resolver
  loadProjects(): void {
    this.loading = true;
    this.projectService.getAllProjects().subscribe({
      next: (data) => {
        this.projects = data;
        if (data && data.length > 0) {
          this.selectedProject = data[0];
          this.onProjectChange();
        }
        this.loading = false;
      },
      error: (err) => {
        this.notification.error('Failed to load projects.');
        this.loading = false;
      }
    });
  }

  onProjectChange(): void {
    if (!this.selectedProject) return;
    this.loadPipelines();
    this.loadDevOpsMetrics();
    this.loadRecentBuilds();
    this.loadReleases();
    this.loadDeployments();
  }

  // Environments
  loadEnvironments(): void {
    this.devopsService.getEnvironments().subscribe({
      next: (data) => this.environments = data,
      error: () => this.notification.error('Failed to load deployment environments.')
    });
  }

  // Pipelines
  loadPipelines(): void {
    if (!this.selectedProject) return;
    this.devopsService.getPipelinesByProject(this.selectedProject.id).subscribe({
      next: (data) => this.pipelines = data,
      error: () => this.notification.error('Failed to load pipelines.')
    });
  }

  // Analytics
  loadDevOpsMetrics(): void {
    if (!this.selectedProject) return;
    this.devopsService.getDevOpsMetrics(this.selectedProject.id).subscribe({
      next: (data) => this.metrics = data,
      error: () => this.notification.error('Failed to load project metrics.')
    });
  }

  // Recent builds
  loadRecentBuilds(): void {
    if (!this.selectedProject) return;
    this.devopsService.getRecentProjectBuilds(this.selectedProject.id).subscribe({
      next: (data) => this.recentBuilds = data,
      error: () => this.notification.error('Failed to load build history.')
    });
  }

  // Releases
  loadReleases(): void {
    if (!this.selectedProject) return;
    this.devopsService.getReleasesByProject(this.selectedProject.id).subscribe({
      next: (data) => this.releases = data,
      error: () => this.notification.error('Failed to load releases catalog.')
    });
  }

  // Deployments
  loadDeployments(): void {
    if (!this.selectedProject) return;
    this.devopsService.getProjectDeployments(this.selectedProject.id).subscribe({
      next: (data) => this.deployments = data,
      error: () => this.notification.error('Failed to load deployment history.')
    });
  }

  // Audits
  loadAuditTimeline(): void {
    this.devopsService.getAuditTimeline().subscribe({
      next: (data) => this.auditLogs = data,
      error: () => this.notification.error('Failed to load audit logs.')
    });
  }

  // Pipeline Modals Action
  openCreatePipeline(): void {
    if (!this.selectedProject) return;
    this.isPipelineEdit = false;
    this.editingPipelineId = undefined;
    this.pipelineForm.reset({
      projectId: this.selectedProject.id,
      branch: 'main',
      buildTool: 'MAVEN',
      pipelineType: 'MOCK',
      pipelineTemplate: 'SPRING_BOOT',
      enabled: true
    });
    this.showPipelineModal = true;
  }

  openEditPipeline(p: Pipeline): void {
    this.isPipelineEdit = true;
    this.editingPipelineId = p.id;
    this.pipelineForm.patchValue({
      name: p.name,
      projectId: p.projectId,
      repositoryUrl: p.repositoryUrl,
      repositoryName: p.repositoryName,
      branch: p.branch,
      buildTool: p.buildTool,
      pipelineType: p.pipelineType,
      pipelineTemplate: p.pipelineTemplate,
      jenkinsJobName: p.jenkinsJobName,
      githubWorkflowPath: p.githubWorkflowPath,
      enabled: p.enabled
    });
    this.showPipelineModal = true;
  }

  submitPipeline(): void {
    if (this.pipelineForm.invalid) return;
    this.loading = true;
    const body = this.pipelineForm.value;

    if (this.isPipelineEdit && this.editingPipelineId) {
      this.devopsService.updatePipeline(this.editingPipelineId, body).subscribe({
        next: () => {
          this.notification.success('Pipeline updated successfully.');
          this.loadPipelines();
          this.loadAuditTimeline();
          this.showPipelineModal = false;
          this.loading = false;
        },
        error: () => {
          this.notification.error('Failed to update pipeline.');
          this.loading = false;
        }
      });
    } else {
      this.devopsService.createPipeline(body).subscribe({
        next: () => {
          this.notification.success('Pipeline configuration created.');
          this.loadPipelines();
          this.loadAuditTimeline();
          this.showPipelineModal = false;
          this.loading = false;
        },
        error: () => {
          this.notification.error('Failed to create pipeline.');
          this.loading = false;
        }
      });
    }
  }

  deletePipeline(id: string): void {
    if (confirm('Are you sure you want to delete this pipeline?')) {
      this.loading = true;
      this.devopsService.deletePipeline(id).subscribe({
        next: () => {
          this.notification.success('Pipeline deleted.');
          this.loadPipelines();
          this.loadAuditTimeline();
          this.loading = false;
        },
        error: () => {
          this.notification.error('Failed to delete pipeline.');
          this.loading = false;
        }
      });
    }
  }

  togglePipeline(p: Pipeline): void {
    this.devopsService.togglePipeline(p.id, !p.enabled).subscribe({
      next: () => {
        this.notification.success(`Pipeline ${!p.enabled ? 'Enabled' : 'Disabled'}.`);
        this.loadPipelines();
        this.loadAuditTimeline();
      },
      error: () => this.notification.error('Failed to update pipeline state.')
    });
  }

  // Environments Modals
  openCreateEnvironment(): void {
    this.isEnvEdit = false;
    this.editingEnvId = undefined;
    this.envForm.reset({ enabled: true });
    this.showEnvModal = true;
  }

  openEditEnvironment(env: Environment): void {
    this.isEnvEdit = true;
    this.editingEnvId = env.id;
    this.envForm.patchValue({
      name: env.name,
      description: env.description,
      enabled: env.enabled
    });
    this.showEnvModal = true;
  }

  submitEnvironment(): void {
    if (this.envForm.invalid) return;
    this.loading = true;
    const body = this.envForm.value;

    if (this.isEnvEdit && this.editingEnvId) {
      this.devopsService.updateEnvironment(this.editingEnvId, body).subscribe({
        next: () => {
          this.notification.success('Environment updated.');
          this.loadEnvironments();
          this.loadAuditTimeline();
          this.showEnvModal = false;
          this.loading = false;
        },
        error: () => {
          this.notification.error('Failed to update environment.');
          this.loading = false;
        }
      });
    } else {
      this.devopsService.createEnvironment(body).subscribe({
        next: () => {
          this.notification.success('Environment created.');
          this.loadEnvironments();
          this.loadAuditTimeline();
          this.showEnvModal = false;
          this.loading = false;
        },
        error: () => {
          this.notification.error('Failed to create environment.');
          this.loading = false;
        }
      });
    }
  }

  toggleEnvironment(env: Environment): void {
    this.devopsService.toggleEnvironment(env.id, !env.enabled).subscribe({
      next: () => {
        this.notification.success(`Environment status toggled.`);
        this.loadEnvironments();
        this.loadAuditTimeline();
      },
      error: () => this.notification.error('Failed to toggle environment status.')
    });
  }

  deleteEnvironment(id: string): void {
    if (confirm('Are you sure you want to delete this environment?')) {
      this.loading = true;
      this.devopsService.deleteEnvironment(id).subscribe({
        next: () => {
          this.notification.success('Environment deleted.');
          this.loadEnvironments();
          this.loadAuditTimeline();
          this.loading = false;
        },
        error: () => {
          this.notification.error('Failed to delete environment.');
          this.loading = false;
        }
      });
    }
  }

  // Trigger Build Run
  triggerPipelineBuild(p: Pipeline): void {
    this.loading = true;
    this.devopsService.triggerBuild(p.id, 'MANUAL').subscribe({
      next: (b) => {
        this.notification.success(`Build #${b.buildNumber} triggered!`);
        this.loading = false;
        // Direct route to visualizer tab
        this.activeTab = 2; 
        this.drilldownBuildLogs(b);
      },
      error: (err) => {
        this.notification.error(err.error?.message || 'Failed to trigger build.');
        this.loading = false;
      }
    });
  }

  // Visualizer Logs Drilldown
  drilldownBuildLogs(b: Build): void {
    this.selectedBuildDetails = b;
    this.selectedStageLog = 'Connecting to builder stream... Fetching live execution frames.';
    this.selectedStageName = 'Build';
    
    // Fetch initial stages
    this.devopsService.getBuildStages(b.id).subscribe({
      next: (stages) => {
        this.selectedBuildStages = stages;
        // Auto-select logs for the first running/success stage
        const activeOrSuccess = stages.find(s => s.status === 'RUNNING' || s.status === 'SUCCESS');
        if (activeOrSuccess) {
          this.selectStage(activeOrSuccess);
        }
        
        // Start polling if build is still running or queued
        if (b.status === 'RUNNING' || b.status === 'QUEUED') {
          this.startBuildPolling(b.id);
        }
      },
      error: () => this.notification.error('Failed to load stages.')
    });
  }

  selectStage(stage: PipelineStage): void {
    this.selectedStageName = stage.name;
    this.selectedStageLog = stage.stageLog ? stage.stageLog : `Stage ${stage.name} is currently ${stage.status}. Log stream is empty.`;
  }

  startBuildPolling(buildId: string): void {
    this.stopBuildPolling();
    this.pollingInterval = setInterval(() => {
      this.devopsService.getBuildStages(buildId).subscribe({
        next: (stages) => {
          this.selectedBuildStages = stages;
          // Refresh active stage logs if user is currently looking at it
          if (this.selectedStageName) {
            const active = stages.find(s => s.name.toUpperCase() === this.selectedStageName.toUpperCase());
            if (active) {
              this.selectedStageLog = active.stageLog ? active.stageLog : `Stage ${active.name} is currently ${active.status}.`;
            }
          }

          // Check if finished
          const hasActive = stages.some(s => s.status === 'RUNNING' || s.status === 'QUEUED');
          if (!hasActive) {
            this.stopBuildPolling();
            this.notification.success('Pipeline execution finished.');
            this.loadDevOpsMetrics();
            this.loadRecentBuilds();
            this.loadPipelines();
          }
        },
        error: () => this.stopBuildPolling()
      });

      this.devopsService.getBuildById(buildId).subscribe({
        next: (b) => this.selectedBuildDetails = b
      });
    }, 2000);
  }

  stopBuildPolling(): void {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = undefined;
    }
  }

  // Trigger Deployment
  openDeployModal(): void {
    if (!this.selectedProject) return;
    this.loading = true;
    
    // Fetch successful builds for this project
    this.devopsService.getRecentProjectBuilds(this.selectedProject.id).subscribe({
      next: (data) => {
        this.deployableBuilds = data.filter(b => b.status === 'SUCCESS');
        if (this.deployableBuilds.length === 0) {
          this.notification.warning('No successful builds found for deployment. Trigger a build first.');
        } else {
          this.deployForm.reset({
            buildId: this.deployableBuilds[0].id,
            version: 'v1.0.0-b' + this.deployableBuilds[0].buildNumber
          });
          this.showDeployModal = true;
        }
        this.loading = false;
      },
      error: () => {
        this.notification.error('Failed to retrieve deployable builds.');
        this.loading = false;
      }
    });
  }

  onDeployBuildChange(event: any): void {
    const buildId = event.target.value;
    const build = this.deployableBuilds.find(b => b.id === buildId);
    if (build) {
      this.deployForm.patchValue({
        version: 'v1.0.0-b' + build.buildNumber
      });
    }
  }

  submitDeployment(): void {
    if (this.deployForm.invalid) return;
    this.loading = true;
    const { environmentId, buildId, version } = this.deployForm.value;

    this.devopsService.triggerDeployment(environmentId, buildId, version).subscribe({
      next: (d) => {
        this.notification.success(`Deployed successfully to ${d.environmentName}!`);
        this.loadDeployments();
        this.loadDevOpsMetrics();
        this.loadAuditTimeline();
        this.showDeployModal = false;
        this.loading = false;
      },
      error: (err) => {
        this.notification.error(err.error?.message || 'Deployment failure.');
        this.loading = false;
      }
    });
  }

  // Rollbacks
  openRollbackModal(d: Deployment): void {
    if (!d.rollbackAvailable) {
      this.notification.error('Rollback is not available for this deployment.');
      return;
    }
    this.rollbackTargetDeploymentId = d.id;
    this.rollbackReason = '';
    this.showRollbackModal = true;
  }

  submitRollback(): void {
    if (!this.rollbackTargetDeploymentId) return;
    this.loading = true;
    
    this.devopsService.rollbackDeployment(this.rollbackTargetDeploymentId, this.rollbackReason).subscribe({
      next: (d) => {
        this.notification.success(`Rollback completed successfully! Current version: ${d.version}`);
        this.loadDeployments();
        this.loadDevOpsMetrics();
        this.loadAuditTimeline();
        this.showRollbackModal = false;
        this.loading = false;
      },
      error: (err) => {
        this.notification.error(err.error?.message || 'Rollback execution failed.');
        this.loading = false;
      }
    });
  }

  // Release Catalog Modals
  openCreateRelease(): void {
    if (!this.selectedProject) return;
    this.loading = true;
    this.isReleaseEdit = false;
    this.editingReleaseId = undefined;

    this.devopsService.getRecentProjectBuilds(this.selectedProject.id).subscribe({
      next: (data) => {
        this.releaseBuilds = data.filter(b => b.status === 'SUCCESS');
        if (this.releaseBuilds.length === 0) {
          this.notification.warning('No successful builds found to release. Build the application first.');
        } else {
          this.releaseForm.reset({
            buildId: this.releaseBuilds[0].id,
            status: 'DRAFT',
            version: '1.0.' + this.releaseBuilds[0].buildNumber
          });
          this.showReleaseModal = true;
        }
        this.loading = false;
      },
      error: () => {
        this.notification.error('Failed to load successful builds.');
        this.loading = false;
      }
    });
  }

  openEditRelease(r: Release): void {
    this.isReleaseEdit = true;
    this.editingReleaseId = r.id;
    this.releaseForm.reset({
      version: r.version,
      name: r.name,
      releaseNotes: r.releaseNotes,
      buildId: r.buildId,
      status: r.status
    });
    this.showReleaseModal = true;
  }

  submitRelease(): void {
    if (this.releaseForm.invalid) return;
    this.loading = true;
    const body = this.releaseForm.value;

    if (this.isReleaseEdit && this.editingReleaseId) {
      this.devopsService.updateRelease(this.editingReleaseId, body).subscribe({
        next: () => {
          this.notification.success('Release updated.');
          this.loadReleases();
          this.loadAuditTimeline();
          this.showReleaseModal = false;
          this.loading = false;
        },
        error: () => {
          this.notification.error('Failed to update release.');
          this.loading = false;
        }
      });
    } else {
      this.devopsService.createRelease(body).subscribe({
        next: () => {
          this.notification.success('Release created.');
          this.loadReleases();
          this.loadAuditTimeline();
          this.showReleaseModal = false;
          this.loading = false;
        },
        error: (err) => {
          this.notification.error(err.error?.message || 'Failed to create release.');
          this.loading = false;
        }
      });
    }
  }

  publishReleaseDirectly(r: Release): void {
    this.loading = true;
    const updateBody = {
      version: r.version,
      name: r.name,
      releaseNotes: r.releaseNotes,
      buildId: r.buildId,
      status: 'RELEASED'
    };
    this.devopsService.updateRelease(r.id, updateBody).subscribe({
      next: () => {
        this.notification.success(`Release version ${r.version} has been published successfully!`);
        this.loadReleases();
        this.loadAuditTimeline();
        this.loading = false;
      },
      error: () => {
        this.notification.error('Failed to publish release.');
        this.loading = false;
      }
    });
  }

  deleteRelease(id: string): void {
    if (confirm('Are you sure you want to delete this release from the catalog?')) {
      this.loading = true;
      this.devopsService.deleteRelease(id).subscribe({
        next: () => {
          this.notification.success('Release deleted.');
          this.loadReleases();
          this.loadAuditTimeline();
          this.loading = false;
        },
        error: () => {
          this.notification.error('Failed to delete release.');
          this.loading = false;
        }
      });
    }
  }

  // Build Compare Action
  submitCompare(): void {
    if (this.compareForm.invalid) return;
    this.loading = true;
    const { buildIdA, buildIdB } = this.compareForm.value;

    this.devopsService.compareBuilds(buildIdA, buildIdB).subscribe({
      next: (data) => {
        this.compareResult = data;
        this.loading = false;
      },
      error: () => {
        this.notification.error('Failed to compare builds.');
        this.loading = false;
      }
    });
  }

  loadBuildsBySelectedPipeline(): void {
    if (!this.selectedPipeline) {
      this.pipelineBuilds = [];
      return;
    }
    this.devopsService.getBuildsByPipeline(this.selectedPipeline.id).subscribe({
      next: (data) => this.pipelineBuilds = data,
      error: () => this.notification.error('Failed to load pipeline builds.')
    });
  }
}
