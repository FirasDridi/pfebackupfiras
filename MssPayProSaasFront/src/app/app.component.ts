import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { KeycloakService } from './modules/keycloak/keycloak.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {
  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.keycloakService.isLoggedIn()) {
      if (this.keycloakService.hasRole('admin')) {
        this.router.navigate(['/admins/groups']);
      } else {
        this.router.navigate(['/user/all-services']); // Ensure it navigates to /user/all-services
      }
    }
  }
}
