import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  if (state.url.startsWith('/admin') && !authService.hasRole('ADMIN')) {
    router.navigate(['/dashboard']);
    return false;
  }
  
  return true;
};
