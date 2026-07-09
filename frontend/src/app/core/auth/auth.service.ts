import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private keycloak!: Keycloak;
  private authenticated = false;

  private decodeJwt(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      console.error('Failed to decode JWT token', e);
      return null;
    }
  }

  async init(): Promise<boolean> {
    this.keycloak = new Keycloak({
      url: 'http://localhost:9000',
      realm: 'neuroforge-realm',
      clientId: 'frontend-client'
    });

    const token = localStorage.getItem('access_token');
    const refreshToken = localStorage.getItem('refresh_token');

    if (token) {
      this.keycloak.token = token;
      this.keycloak.refreshToken = refreshToken || undefined;
      const parsed = this.decodeJwt(token);
      this.keycloak.tokenParsed = parsed;
      this.keycloak.subject = parsed?.sub;
      this.keycloak.realmAccess = parsed?.realm_access;
    }

    try {
      this.authenticated = await this.keycloak.init({
        token: token || undefined,
        refreshToken: refreshToken || undefined,
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        checkLoginIframe: false
      });
      
      if (this.authenticated) {
        if (this.keycloak.token) localStorage.setItem('access_token', this.keycloak.token);
        if (this.keycloak.refreshToken) localStorage.setItem('refresh_token', this.keycloak.refreshToken);
        await this.syncProfile();
      }
      return this.authenticated;
    } catch (error) {
      console.error('Keycloak initialization failed', error);
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      this.authenticated = false;
      return false;
    }
  }

  async loginWithCredentials(username: string, password: string, verificationCode: string): Promise<boolean> {
    const body = new URLSearchParams();
    body.set('client_id', 'frontend-client');
    body.set('username', username);
    body.set('password', password);
    body.set('grant_type', 'password');

    try {
      const response = await fetch('http://localhost:9000/realms/neuroforge-realm/protocol/openid-connect/token', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: body.toString()
      });

      if (!response.ok) {
        throw new Error('Invalid username or password');
      }

      const data = await response.json();
      const parsed = this.decodeJwt(data.access_token);
      
      // Map verification codes to their corresponding roles
      const codeToRoleMap: { [key: string]: string[] } = {
        'A0621': ['ADMIN'],
        'D0621': ['DEVELOPER'],
        'SD0621': ['DEVELOPER'],
        'OW0621': ['ORGANIZATION_OWNER'],
        'TL0621': ['TEAM_LEAD'],
        'QA0621': ['QA'],
        'S0621': ['STAKEHOLDER']
      };

      const roles: string[] = parsed?.realm_access?.roles || [];
      const authorizedRoles = codeToRoleMap[verificationCode];
      const hasValidRole = authorizedRoles && authorizedRoles.some(role => roles.includes(role));

      if (!hasValidRole) {
        throw new Error('Invalid Two-Step Verification Code for your role.');
      }

      localStorage.setItem('access_token', data.access_token);
      localStorage.setItem('refresh_token', data.refresh_token);

      // Populate Keycloak instance fields manually
      this.keycloak.token = data.access_token;
      this.keycloak.refreshToken = data.refresh_token;
      
      this.keycloak.tokenParsed = parsed;
      this.keycloak.subject = parsed?.sub;
      this.keycloak.realmAccess = parsed?.realm_access;
      
      this.authenticated = true;

      await this.syncProfile();
      return true;
    } catch (error: any) {
      console.error('Login failed', error);
      throw error;
    }
  }

  async registerUser(payload: any): Promise<any> {
    const response = await fetch('http://localhost:8080/api/v1/users/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      const msg = errorData?.message || 'Registration failed';
      throw new Error(msg);
    }
    return await response.json();
  }

  login(): void {
    // Fallback redirect login if needed
    this.keycloak.login();
  }

  logout(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    this.authenticated = false;
    this.keycloak.logout({ redirectUri: window.location.origin + '/login' });
  }

  getToken(): string {
    return this.keycloak.token || '';
  }

  isAuthenticated(): boolean {
    return this.authenticated;
  }

  getUsername(): string {
    return this.keycloak.tokenParsed?.['preferred_username'] || '';
  }

  getEmail(): string {
    return this.keycloak.tokenParsed?.['email'] || '';
  }

  getName(): string {
    const given = this.keycloak.tokenParsed?.['given_name'] || '';
    const family = this.keycloak.tokenParsed?.['family_name'] || '';
    return given || family ? `${given} ${family}`.trim() : this.getUsername();
  }

  getUserId(): string {
    return this.keycloak.subject || '';
  }

  getRoles(): string[] {
    return this.keycloak.realmAccess?.roles || [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  private async syncProfile(): Promise<void> {
    const body = {
      id: this.getUserId(),
      username: this.getUsername(),
      email: this.getEmail(),
      firstName: this.keycloak.tokenParsed?.['given_name'] || '',
      lastName: this.keycloak.tokenParsed?.['family_name'] || ''
    };

    try {
      const response = await fetch('http://localhost:8080/api/v1/users/sync', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.getToken()}`
        },
        body: JSON.stringify(body)
      });
      if (!response.ok) {
        console.error('Failed to sync profile with User Service');
      }
    } catch (err) {
      console.error('Error syncing profile with User Service', err);
    }
  }
}
