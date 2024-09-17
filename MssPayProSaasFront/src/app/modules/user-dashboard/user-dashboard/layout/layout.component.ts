import { Component, OnInit } from '@angular/core';
import { KeycloakService } from '../../../keycloak/keycloak.service';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.css'],
})
export class LayoutComponent implements OnInit {
  title: string = 'User MssPayProSaas';
  isLoggedIn: boolean = false;
  isSuperUser: boolean = false;
  menuItems: any[] = [];

  constructor(private keycloakService: KeycloakService, private router: Router) {}

  ngOnInit(): void {
    this.isLoggedIn = this.keycloakService.isLoggedIn();
    this.isSuperUser = this.keycloakService.hasRole('superuser');
    this.setupMenuItems();

    this.menuItems = this.menuItems.filter(item => item.visible !== false);

    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      const currentRoute = this.router.url.split('/').pop();
      this.updateTitle(currentRoute);
    });
  }

  setupMenuItems(): void {
    if (!this.isSuperUser) {
      this.menuItems = [
        { label: 'Profile', icon: 'person', routerLink: 'profile', visible: this.isLoggedIn },
        { label: 'Services', icon: 'view_module', routerLink: 'services', visible: this.isLoggedIn },
      ];
    }

    if (this.isSuperUser) {
      this.menuItems = [
        { label: 'Dashboard', icon: 'dashboard', routerLink: 'user-dashboard', visible: this.isLoggedIn },
        { label: 'Profile', icon: 'person', routerLink: 'profile', visible: this.isLoggedIn },
        { label: 'Services', icon: 'view_module', routerLink: 'services', visible: this.isLoggedIn },
        { label: 'Service Usage', icon: 'history', routerLink: 'service-usage', visible: this.isLoggedIn },
        { label: 'All Services', icon: 'view_list', routerLink: 'all-services', visible: this.isSuperUser },
      ];
    }
  }

  login(): void {
    this.keycloakService.login();
  }

  logout(): void {
    try {
      this.keycloakService.logout();
      this.router.navigate(['/login']);
    } catch (error) {
      console.error('Error during logout:', error);
    }
  }

  private updateTitle(route: string | undefined): void {
    switch (route) {
      case 'user-dashboard':
        this.title = 'Dashboard - User MssPayProSaas';
        break;
      case 'profile':
        this.title = 'Profile - User MssPayProSaas';
        break;
      case 'services':
        this.title = 'Services - User MssPayProSaas';
        break;
      case 'service-usage':
        this.title = 'Service Usage - User MssPayProSaas';
        break;
      case 'all-services':
        this.title = 'All Services - User MssPayProSaas';
        break;
      default:
        this.title = 'User MssPayProSaas';
    }
  }
}
