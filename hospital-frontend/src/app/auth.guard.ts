import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem('auth_token');
  const user = localStorage.getItem('auth_user');

  if (token && user) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};
