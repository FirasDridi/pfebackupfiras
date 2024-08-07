// src/app/user-dashboard/user-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { KeycloakService } from '../keycloak/keycloak.service';
import { UserService } from '../user/user.service';
import { UserDTO } from '../user/user.dto';
import { GroupDto } from '../group/group.dto';
import { MenuItem, PrimeNGConfig } from 'primeng/api';
import { MatTableDataSource } from '@angular/material/table';
import { Router, NavigationEnd, Event } from '@angular/router';
import { filter } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { GroupService } from '../group/group-service.service'; // Import GroupService
import { UserInvoicesComponent } from '../facture/user-invoices/user-invoices.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-user-dashboard',
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.css'],
})
export class UserDashboardComponent implements OnInit {
  title: string = 'User Dashboard';
  isLoggedIn: boolean = false;
  isMobile: boolean = false;
  isCollapsed: boolean = true;
  menuItems: MenuItem[] = [];
  user: UserDTO | null = null; // Store user details
  userGroups: GroupDto[] = []; // Store user groups
  usersInGroups: { [groupId: string]: UserDTO[] } = {}; // Store users in groups
  displayedUserColumns: string[] = ['username', 'firstname', 'lastname', 'email', 'actions'];
  isSuperUser: boolean = false; // To check if the user is a superuser
  addUserForm!: FormGroup; // Add form group
  selectedGroup: string | null = null;

  constructor(
    private keycloakService: KeycloakService,
    private userService: UserService,
    private primengConfig: PrimeNGConfig,
    public router: Router,
    private snackBar: MatSnackBar,
    private http: HttpClient,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private groupService: GroupService // Add GroupService to constructor
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.keycloakService.isLoggedIn();
    this.isMobile = window.innerWidth < 768;
    this.primengConfig.ripple = true;

    this.addUserForm = this.fb.group({
      groupName: [{ value: '', disabled: true }, Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    this.menuItems = [
      { label: 'Dashboard', icon: 'pi pi-fw pi-home', routerLink: '/user-dashboard' },
      { label: 'Profile', icon: 'pi pi-fw pi-user', routerLink: '/profile' },
      { label: 'Services', icon: 'pi pi-fw pi-list', routerLink: '/services' },
      { separator: true },
      { label: 'Login', icon: 'pi pi-fw pi-sign-in', command: () => this.login(), visible: !this.isLoggedIn },
      { label: 'Logout', icon: 'pi pi-fw pi-sign-out', command: () => this.logout(), visible: this.isLoggedIn },
      { label: 'My Account', icon: 'pi pi-fw pi-user-edit', command: () => this.accountManagement(), visible: this.isLoggedIn },
    ];

    if (this.isLoggedIn) {
      const userId = this.keycloakService.getUserId(); // Get the logged-in user ID
      if (userId) {
        this.loadUserDetails(userId); // Load user details and groups when logged in
        this.isSuperUser = this.keycloakService.hasRole('superuser'); // Check if the user is a superuser
        if (this.isSuperUser) {
          console.log('Super user logged in, loading users in groups...');
          this.loadUsersInGroups();
        }
      } else {
        console.error('User ID is undefined');
      }
    }

    this.router.events.pipe(filter((event: Event): event is NavigationEnd => event instanceof NavigationEnd)).subscribe((event: NavigationEnd) => {
      if (event.url === '/services' && this.isLoggedIn) {
        this.loadServicesForAllGroups();
      }
    });

    window.addEventListener('resize', this.onResize.bind(this));
  }

  loadServicesForAllGroups() {
    throw new Error('Method not implemented.');
  }

  toggleMenu(): void {
    this.isCollapsed = !this.isCollapsed;
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

  onResize(): void {
    this.isMobile = window.innerWidth < 768;
  }

  loadUserDetails(userId: string): void {
    this.userService.getUserDetails(userId).subscribe(
      (response) => {
        this.user = response.user;
        this.userGroups = response.groups;
        if (this.isSuperUser) {
          console.log('Loading users in groups for superuser...');
          this.loadUsersInGroups();
        }
      },
      (error) => {
        console.error('Error loading user details', error);
      }
    );
  }

  toggleAddUserForm(groupName: string): void {
    this.selectedGroup = this.selectedGroup === groupName ? null : groupName;
  }

  onSubmit(groupName: string): void {
    if (this.addUserForm.valid) {
      const userGroupDTO = {
        groupName,
        firstname: this.addUserForm.get('firstName')?.value,
        lastName: this.addUserForm.get('lastName')?.value,
        emailId: this.addUserForm.get('email')?.value,
        password: this.addUserForm.get('password')?.value,
        userName: this.addUserForm.get('email')?.value // Assuming username is the email
      };

      const headers = new HttpHeaders({
        'Authorization': `Bearer ${this.keycloakService.getToken()}`,
        'Content-Type': 'application/json'
      });

      this.http.post<any>(`http://localhost:8884/admin/addUsersToGroup?groupName=${groupName}`, [userGroupDTO], { headers }).subscribe(
        (response) => {
          console.log(`User added successfully to group ${groupName}`, response);
          this.loadUsersInGroups(); // Reload users in groups after adding a new user
          this.snackBar.open(`User added to group ${groupName} successfully`, 'Close', { duration: 3000 });
          this.selectedGroup = null; // Reset the selected group after submission
        },
        (error) => {
          console.error(`Error adding user to group ${groupName}:`, error);
          this.snackBar.open(`Failed to add user to group ${groupName}`, 'Close', { duration: 3000 });
        }
      );
    } else {
      this.snackBar.open('Please fill out the form', 'Close', { duration: 3000 });
    }
  }

  loadUsersInGroups(): void {
    this.userGroups.forEach(group => {
      if (group.id !== undefined) {
        this.groupService.getGroupUsers(group.id.toString()).subscribe(users => {
          console.log(`Loaded users for group ${group.id}:`, users);
          if (group.id !== undefined) {
            this.usersInGroups[group.id] = users as UserDTO[];
          }
        }, error => {
          console.error(`Error loading users for group ${group.id}:`, error);
        });
      }
    });
  }

  viewUserInvoices(userId: string): void {
    const dialogRef = this.dialog.open(UserInvoicesComponent, {
      width: '600px',
      data: { userId: userId }
    });

    dialogRef.afterClosed().subscribe(result => {
      // Handle dialog close if necessary
    });
  }
}
