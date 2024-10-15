import { Component, OnInit } from '@angular/core';
import { KeycloakService } from '../../../keycloak/keycloak.service';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { LoadingDialogComponent } from '../../loading-dialog/loading-dialog.component';
import { MatDialog } from '@angular/material/dialog';

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

  constructor(
    private keycloakService: KeycloakService,
    private router: Router,
    private dialog: MatDialog // Inject MatDialog for showing the loading dialog
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.keycloakService.isLoggedIn();
    this.isSuperUser = this.keycloakService.hasRole('superuser');
    this.setupMenuItems();

    // Filter out menu items that are not visible
    this.menuItems = this.menuItems.filter(item => item.visible !== false);

    // Redirect to profile on first load if logged in
    if (this.isLoggedIn) {
      this.router.navigate(['/user/profile']);
    }

    // Update the title based on the current route
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
        { label: 'Users', icon: 'people', routerLink: 'user-dashboard', visible: this.isLoggedIn },
        { label: 'Profile', icon: 'person', routerLink: 'profile', visible: this.isLoggedIn },
        { label: 'All Services', icon: 'view_list', routerLink: 'all-services', visible: this.isSuperUser },
        { label: 'Statistics', icon: 'bar_chart', routerLink: 'statistics', visible: this.isLoggedIn },
        { label: 'Invoices', icon: 'receipt', routerLink: 'user-invoices-new', visible: this.isSuperUser } // Update the routerLink to 'user-invoices-new'
      ];
    }
  }

  navigateToTab(index: number): void {
    if (index < this.menuItems.length) {
      const selectedItem = this.menuItems[index];
      this.router.navigate([`/user/${selectedItem.routerLink}`]);
    }
  }

  login(): void {
    this.keycloakService.login();
  }

  logout(): void {
    try {
      // Open a loading dialog to show a spinner while logging out
      const loadingDialogRef = this.dialog.open(LoadingDialogComponent, {
        disableClose: true, // User cannot close manually
        data: { message: 'Logging out...' }
      });

      // Perform logout
      this.keycloakService.logout();

      // Close the loading spinner and redirect after logout
      loadingDialogRef.close();
      this.router.navigate(['/login']);
    } catch (error) {
      console.error('Unexpected error during logout:', error);
      alert('Logout failed. Please try again.');
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
      case 'statistics':
        this.title = 'Statistics - User MssPayProSaas';
        break;
      case 'invoices':
        this.title = 'Invoices - User MssPayProSaas';
        break;
      default:
        this.title = 'User MssPayProSaas';
    }
  }
}
