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
     // Récupérer le token CSRF si nécessaire
  this.keycloakService.getCsrfToken().subscribe(
    () => {
      console.log('Token CSRF récupéré avec succès');
    },
    error => {
      console.error('Erreur lors de la récupération du token CSRF', error);
    });
    if (this.keycloakService.isLoggedIn()) {
      if (this.keycloakService.hasRole('admin')) {
        this.router.navigate(['/admins/groups']);
      } else {
        this.router.navigate(['/user/profile']); // Ensure it navigates to /user/all-services
      }
    }
  }
}
