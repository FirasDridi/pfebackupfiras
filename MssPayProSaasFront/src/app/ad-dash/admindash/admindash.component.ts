import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AddGroupComponent } from '../../modules/group/add-group/add-group.component';
import { AddUserComponent } from '../../modules/user/add-user/add-user.component';
import { KeycloakService } from '../../modules/keycloak/keycloak.service';
import { Router } from '@angular/router';
import { BooleanInput } from '@angular/cdk/coercion';
import { BreakpointObserver } from '@angular/cdk/layout';
import { NotificationService } from './notifactionRequest/notification-service.service';
import { AdminService } from './notifactionRequest/admin-service.service';
import { Notification } from './notifactionRequest/notifications/notifications/notifications.module';

@Component({
  selector: 'app-admindash',
  templateUrl: './admindash.component.html',
  styleUrls: ['./admindash.component.css'],
})
export class AdmindashComponent implements OnInit, OnDestroy {
  serviceForm!: FormGroup;
  title = 'MssPayProSaas';
  isMobile = true;
  isCollapsed = true;
  isLoggedIn = false;
  loggedInUser: any;
  isSidenavOpen: BooleanInput;
  menuItems: any[] = [];
  apis: any[] = [];
  notifications: Notification[] = [];
  unreadCount = 0;
  requests: any[] = [];
  isLoading = false;
  successMessage: string | null = null;
  successIcon: string | null = null;
  notificationInterval: any;

  constructor(
    private http: HttpClient,
    public router: Router,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar,
    private observer: BreakpointObserver,
    private dialog: MatDialog,
    private keycloakService: KeycloakService,
    private notificationService: NotificationService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.observer.observe(['(max-width: 800px)']).subscribe((screenSize) => {
      this.isMobile = screenSize.matches;
    });

    this.isLoggedIn = this.keycloakService.isLoggedIn();

    if (this.isLoggedIn) {
      this.keycloakService.getUserProfile().then((profile) => {
        this.loggedInUser = profile;
        this.setMenuItems();
        this.loadNotificationsAndRequests();
        // Removed navigation to '/admins/list' to allow default routing
        this.router.navigate(['/admins/statistics']);

        // Reload notifications every 5 seconds
        this.notificationInterval = setInterval(() => {
          this.loadNotificationsAndRequests();
        }, 5000);
      });
    } else {
      this.login();
    }
  }

  ngOnDestroy(): void {
    if (this.notificationInterval) {
      clearInterval(this.notificationInterval);
    }
  }

  setMenuItems(): void {
    this.menuItems = [
      { title: 'Users', icon: 'people', path: '/admins/userlist' },
      { title: 'Clients', icon: 'group', path: '/admins/groups' },
      { title: 'Add Client', icon: 'group_add', path: '/admins/group/add-group' },
      { title: 'APIs', icon: 'api', path: '/admins/list' },
      { title: 'Statistics', icon: 'bar_chart', path: '/admins/statistics' } // New menu item
    ];
  }

  toggleMenu(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  initializeForm(): void {
    this.serviceForm = this.formBuilder.group({
      name: ['', Validators.required],
      description: [''],
      version: ['', Validators.required],
      endpoint: [
        '',
        [
          Validators.required,
          Validators.pattern(/^(http|https):\/\/[^\s$.?#].[^\s]*$/),
        ],
      ],
      configuration: [''],
      pricing: ['', Validators.required],
      status: [false],
    });
  }

  openAddGroupPopup(): void {
    const dialogRef = this.dialog.open(AddGroupComponent, {
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const confirmed = confirm('Client added successfully. Do you want to add users to this ?');
        if (confirmed) {
          this.openAddUserPopupWithGroupId(result);
        }
      }
    });
  }

  openAddUserPopupWithGroupId(groupId: string): void {
    const dialogRef = this.dialog.open(AddUserComponent, {
      width: '400px',
      data: { groupId: groupId },
    });

    dialogRef.afterClosed().subscribe((result) => {
      // Handle the result if needed
    });
  }

  login(): void {
    this.keycloakService.login();
  }

  logout(): void {
    this.keycloakService.logout();
  }

  accountManagement(): void {
    this.router.navigate(['admins/my-account']);
  }

  onNavItemClicked(): void {
    if (this.isMobile) {
      this.isSidenavOpen = false;
    }
  }

  loadNotificationsAndRequests(): void {
    this.notificationService.getNotifications().subscribe((notifications) => {
      this.adminService.getPendingRequests().subscribe((requests) => {
        this.requests = requests.filter(request => request.status === 'PENDING');

        // Map notifications to their corresponding requests
        this.notifications = notifications.map(notification => {
          const request = this.requests.find(req => req.id === notification.id);
          if (request) {
            notification.serviceName = request.serviceName;

            // Fetch group details directly
            if (request.groupId) {
              this.http.get<any>(`http://localhost:8884/api/v1/groups/details/${request.groupId}`).subscribe((group) => {
                notification.groupName = group.name;  // Add group name to the notification
              });
            }
          }

          // Ensure user details are correctly assigned
          if (!notification.user || !notification.user.username) {
            notification.user = {
              ...notification.user,
              username: notification.user?.username ?? 'Unknown User',
              firstname: notification.user?.firstname ?? ''
            };
          }
          return notification;
        });
        this.unreadCount = this.notifications.length;
      });
    });
  }

  approveRequest(notificationId: number): void {
    this.isLoading = true;
    this.adminService.approveRequest(notificationId).subscribe(() => {
      this.notifications = this.notifications.filter(n => n.id !== notificationId);
      this.unreadCount = this.notifications.length;
      this.isLoading = false;
      this.successMessage = "Subscription request approved successfully";
      this.successIcon = "check_circle";
      this.showSnackBar(this.successMessage, this.successIcon);
      setTimeout(() => this.loadNotificationsAndRequests(), 3000);
    }, error => {
      console.error('Error approving request:', error);
      this.isLoading = false;
      this.successMessage = "Failed to approve request";
      this.successIcon = "error";
      this.showSnackBar(this.successMessage, this.successIcon);
    });
  }

  rejectRequest(notificationId: number): void {
    this.isLoading = true;
    this.adminService.rejectRequest(notificationId).subscribe(() => {
      this.notifications = this.notifications.filter(n => n.id !== notificationId);
      this.unreadCount = this.notifications.length;
      this.isLoading = false;
      this.successMessage = "Subscription request rejected successfully";
      this.successIcon = "cancel";
      this.showSnackBar(this.successMessage, this.successIcon);
      setTimeout(() => this.loadNotificationsAndRequests(), 3000);
    }, error => {
      console.error('Error rejecting request:', error);
      this.isLoading = false;
      this.successMessage = "Failed to reject request";
      this.successIcon = "error";
      this.showSnackBar(this.successMessage, this.successIcon);
    });
  }

  showSnackBar(message: string, icon: string): void {
    this.snackBar.open(message, '', {
      duration: 1000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: ['snack-bar-success'],
    });
  }
}
