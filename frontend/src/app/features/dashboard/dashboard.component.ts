import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UserService, User } from '../../core/services/user.service';
import { ProjectService } from '../../core/services/project.service';
import { SprintService } from '../../core/services/sprint.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private userService = inject(UserService);
  private projectService = inject(ProjectService);
  private sprintService = inject(SprintService);
  authService = inject(AuthService);

  userProfile?: User;
  stats = {
    projectsCount: 0,
    teamsCount: 0,
    usersCount: 0,
    sprintsCount: 0
  };
  recentProjects: any[] = [];
  loading = true;

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    
    // Fetch profile, stats, and sprints in parallel
    forkJoin({
      me: this.userService.getMe(),
      projects: this.projectService.getAllProjects(),
      teams: this.userService.getAllTeams(),
      users: this.userService.getAllUsers(),
      sprints: this.sprintService.getAllSprints().pipe(catchError(() => of([])))
    }).subscribe({
      next: (res) => {
        this.userProfile = res.me;
        this.stats.projectsCount = res.projects.length;
        this.stats.teamsCount = res.teams.length;
        this.stats.usersCount = res.users.length;
        this.stats.sprintsCount = res.sprints.length;
        // Take recent projects to display on dashboard
        this.recentProjects = res.projects.slice(0, 3);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard metrics', err);
        this.loading = false;
      }
    });
  }
}
