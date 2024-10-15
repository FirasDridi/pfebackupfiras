// File Path: src/app/home/home.component.ts

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { KeycloakService } from '../modules/keycloak/keycloak.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  isLoggedIn = false;

  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.keycloakService.isLoggedIn();
    // Remove or comment out the redirection code
    /*
    if (this.isLoggedIn) {
      if (this.keycloakService.hasRole('admin')) {
        this.router.navigate(['/admins']);
      } else {
        this.router.navigate(['/user/services']);
      }
    }
    */
  }



  login(): void {
    this.keycloakService.login();
  }
}
