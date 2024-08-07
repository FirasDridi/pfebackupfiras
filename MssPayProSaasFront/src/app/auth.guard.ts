import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';  // Add CanActivate import
import { KeycloakService } from './modules/keycloak/keycloak.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  canActivate(): boolean {
    if (this.keycloakService.isLoggedIn()) {
      if (this.keycloakService.hasRole('admin')) {
        this.router.navigate(['/admins/groups']);
        return true;
      } else {
        return true; // Allow non-admin users to access non-admin routes
      }
    } else {
      this.router.navigate(['/api/list']);
      return false;
    }
  }
}
