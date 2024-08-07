import { Component, OnInit } from '@angular/core';
import { KeycloakService } from '../../../keycloak/keycloak.service';
import { Router } from '@angular/router';

interface MenuItem {
  label: string;
  icon: string;
  routerLink: string;
  command?: () => void;
  visible?: boolean;
}

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.css'],
})
export class LayoutComponent implements OnInit {
  title: string = 'User MssPayProSaas';
  isLoggedIn: boolean = false;
  menuItems: MenuItem[] = [];

  constructor(private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    this.isLoggedIn = this.keycloakService.isLoggedIn();

    this.menuItems = [
      { label: 'Dashboard', icon: 'home', routerLink: 'user-dashboard' },
      { label: 'Profile', icon: 'person', routerLink: 'profile' },
      { label: 'Services', icon: 'list', routerLink: 'services' },
      {
        label: 'All Services',
        icon: 'view_module',
        routerLink: 'all-services',
      },
      {
        label: 'Login',
        icon: 'login',
        command: () => this.login(),
        visible: !this.isLoggedIn,
        routerLink: 'login',
      },
      {
        label: 'Logout',
        icon: 'logout',
        command: () => this.logout(),
        visible: this.isLoggedIn,
        routerLink: 'logout',
      },
      {
        label: 'My Account',
        icon: 'account_circle',
        command: () => this.accountManagement(),
        visible: this.isLoggedIn,
        routerLink: 'account',
      },
    ];
  }

  login(): void {
    this.keycloakService.login();
  }

  logout(): void {
    this.keycloakService.logout();
  }

  accountManagement(): void {
    // Redirect to account management page or perform account management actions
  }

  handleAction(item: any) {
    console.log({ item });
  }
}
