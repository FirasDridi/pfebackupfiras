import { Component, OnInit } from '@angular/core';
import { KeycloakService } from '../keycloak/keycloak.service';
import { UserService } from '../user/user.service';
import { UserDTO } from '../user/user.dto';
import { GroupDto } from '../group/group.dto';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { GroupService } from '../group/group-service.service';
import { UserInvoicesComponent } from '../facture/user-invoices/user-invoices.component';
import { AddUserDialogComponent } from './user-dashboard/add-user-dialog/add-user-dialog.component';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-user-dashboard',
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.css'],
})
export class UserDashboardComponent implements OnInit {
  isLoggedIn: boolean = false;
  menuItems: MenuItem[] = [];
  user: UserDTO | null = null;
  userGroups: GroupDto[] = [];
  usersInGroups: { [groupId: string]: UserDTO[] } = {};
  displayedUserColumns: string[] = ['username', 'firstname', 'lastName', 'email', 'actions'];
  isSuperUser: boolean = false;
  loggedInUserId: string | null = null;
  selectedGroupId: string | null = null;
  selectedGroupName: string | null = null;

  constructor(
    private keycloakService: KeycloakService,
    private userService: UserService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private http: HttpClient,
    private dialog: MatDialog,
    private groupService: GroupService
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.keycloakService.isLoggedIn();
    this.isSuperUser = this.keycloakService.hasRole('superuser');
    console.log('Is Superuser:', this.isSuperUser); // Add this line to log the superuser status

    if (this.isLoggedIn) {
      this.loggedInUserId = this.keycloakService.getUserId() || null;
      console.log('Logged in user ID:', this.loggedInUserId);  // Logging the logged-in user's ID
      if (this.loggedInUserId) {
        this.loadUserDetails(this.loggedInUserId);
        if (this.isSuperUser) {
          this.loadUsersInGroups();
        }
      }
    }
  }


  openAddUserDialog(): void {
    const dialogRef = this.dialog.open(AddUserDialogComponent, {
      width: '400px',
      data: { groupName: this.selectedGroupName }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'success') {
        this.loadUsersInGroups();
      }
    });
  }

  loadUserDetails(userId: string): void {
    this.userService.getUserDetails(userId).subscribe(
      response => {
        this.user = response.user;
        this.userGroups = response.groups;
        if (this.isSuperUser) {
          this.selectedGroupId = this.userGroups[0]?.id?.toString() || null;
          this.selectedGroupName = this.userGroups[0]?.groupName || null;
          this.loadUsersInGroups();
        }
      },
      error => {
        console.error('Error loading user details', error);
      }
    );
  }

  loadUsersInGroups(): void {
    if (this.selectedGroupId) {
        this.groupService.getGroupUsers(this.selectedGroupId).subscribe(userGroups => {
            console.log('Users in group before filtering:', userGroups);  // Logging users before filtering
            this.usersInGroups[this.selectedGroupId!] = userGroups
                .filter(userGroup => {
                    console.log('Comparing:', userGroup.id?.toString(), 'with', this.loggedInUserId); // Log comparison
                    return userGroup.id?.toString() !== this.loggedInUserId;
                })
                .map(userGroup => ({
                    ...userGroup,
                    id: userGroup.id?.toString(),
                    isSuperUser: userGroup.roles.includes('superuser') // Ensure this line checks the roles correctly
                })) as UserDTO[];

            console.log('Filtered Users:', this.usersInGroups[this.selectedGroupId!]);  // Logging users after filtering
        }, error => {
            console.error(`Error loading users for group ${this.selectedGroupId}:`, error);
        });
    }
}




  viewUserInvoices(userId: string): void {
    const user = this.usersInGroups[this.selectedGroupId!]?.find(u => u.id === userId || u.keycloakId === userId);
    if (user && user.keycloakId) {
      const dialogRef = this.dialog.open(UserInvoicesComponent, {
        width: '600px',
        data: { userId: user.keycloakId }
      });

      dialogRef.afterClosed().subscribe(result => {
        // Handle dialog close if necessary
      });
    } else {
      console.error('User not found or Keycloak ID missing.');
    }
  }
}
