// File Path: src/app/app.component.ts

import { Component, OnInit } from '@angular/core';
import { KeycloakService } from './modules/keycloak/keycloak.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {
  constructor(private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    // Retrieve the CSRF token if necessary
    this.keycloakService.getCsrfToken().subscribe(
      () => {
        console.log('CSRF token retrieved successfully');
      },
      (error) => {
        console.error('Error retrieving CSRF token', error);
      }
    );

    // Remove automatic navigation to allow the home page to load
    /*
    if (this.keycloakService.isLoggedIn()) {
      if (this.keycloakService.hasRole('admin')) {
        this.router.navigate(['/admins/groups']);
      } else {
        this.router.navigate(['/user/profile']);
      }
    }
    */
  }
}
