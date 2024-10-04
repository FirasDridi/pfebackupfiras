// File Path: src/app/modules/user-dashboard/user-dashboard.component.ts

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { KeycloakService } from '../keycloak/keycloak.service';
import { UserService } from '../user/user.service';
import { UserDTO } from '../user/user.dto';
import { GroupDto } from '../group/group.dto';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder } from '@angular/forms';
import { GroupService } from '../group/group-service.service';
import { UserInvoicesComponent } from '../facture/user-invoices/user-invoices.component';
import { AddUserDialogComponent } from './user-dashboard/add-user-dialog/add-user-dialog.component';
import { EditUserComponent } from '../user/edit-user/edit-user.component';
import {
  DeleteConfirmationDialogComponent,
  DeleteConfirmationDialogData,
} from '../api-service/add-api/delete-confirmation-dialog/delete-confirmation-dialog.component';
import { MenuItem } from 'primeng/api';
import { forkJoin, of } from 'rxjs';
import { catchError, map, mergeMap } from 'rxjs/operators';
import { UserGroupDTO } from '../group/group/UserGroupDTO';

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
  isLoading: boolean = false; // For the spinner

  constructor(
    private keycloakService: KeycloakService,
    private userService: UserService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private groupService: GroupService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.keycloakService.isLoggedIn();
    this.isSuperUser = this.keycloakService.hasRole('superuser');
    console.log('Is Superuser:', this.isSuperUser);

    if (this.isLoggedIn) {
      this.loggedInUserId = this.keycloakService.getUserId() || null;
      console.log('Logged in user ID:', this.loggedInUserId);
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
      data: { groupName: this.selectedGroupName },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'success') {
        this.loadUsersInGroups();
        this.snackBar.open('User added successfully.', 'Close', { duration: 3000 });
      }
    });
  }

  loadUserDetails(userId: string): void {
    this.userService.getUserDetails(userId).subscribe(
      (response) => {
        this.user = response.user;
        this.userGroups = response.groups;
        if (this.isSuperUser && this.userGroups.length > 0) {
          this.selectedGroupId = this.userGroups[0]?.id?.toString() || null;
          this.selectedGroupName = this.userGroups[0]?.groupName || null;
          this.loadUsersInGroups();
        }
      },
      (error) => {
        console.error('Error loading user details', error);
        this.snackBar.open('Failed to load user details.', 'Close', { duration: 3000 });
      }
    );
  }

  loadUsersInGroups(): void {
    const groupId = this.selectedGroupId;

    if (typeof groupId === 'string' && groupId.trim() !== '') {
      const validGroupId: string = groupId;
      this.isLoading = true; // Start loading

      this.groupService
        .getGroupUsers(validGroupId)
        .pipe(
          mergeMap((userGroups: UserGroupDTO[]) => {
            // Filter out the logged-in user
            const filteredUsers = userGroups.filter(
              (userGroup) => userGroup.id?.toString() !== this.loggedInUserId
            );

            if (filteredUsers.length === 0) {
              return of([]); // Return empty array if no users
            }

            // Fetch details from Keycloak for each user
            const userDetailsObservables = filteredUsers.map((user) => {
              const keycloakId = user.keycloakId;
              if (keycloakId) {
                return this.userService.getUserDetailsFromKeycloak(keycloakId).pipe(
                  catchError((err) => {
                    console.error(`Error fetching details for user ${user.id ?? 'unknown'}:`, err);
                    return of(null); // Return null if error occurs
                  })
                );
              } else {
                console.warn(`User ${user.id ?? 'unknown'} does not have a keycloakId.`);
                return of(null);
              }
            });

            return forkJoin(userDetailsObservables).pipe(
              map((userDetails) => {
                // Merge Keycloak details with existing user data
                return filteredUsers.map((user, index) => {
                  const keycloakDetails = userDetails[index];
                  return {
                    ...user,
                    lastName: user.lastName || user.lastname,
                    firstName: user.firstName || user.firstname,
                    superUser: keycloakDetails?.roles?.includes('superuser') || false,
                    roles: keycloakDetails?.roles || [],
                    // Merge other properties from Keycloak if needed
                  } as UserDTO;
                });
              })
            );
          })
        )
        .subscribe(
          (usersWithDetails) => {
            this.usersInGroups[validGroupId] = usersWithDetails;
            this.isLoading = false; // Loading complete
            this.cdr.detectChanges();
          },
          (error) => {
            console.error(`Error loading users for group ${validGroupId}:`, error);
            this.snackBar.open(`Failed to load users for group ${validGroupId}.`, 'Close', {
              duration: 3000,
            });
            this.isLoading = false; // Loading complete on error
          }
        );
    } else {
      console.warn('Selected Group ID is null or invalid. Cannot load users.');
      this.snackBar.open('No valid group selected.', 'Close', { duration: 3000 });
    }
  }

  viewUserInvoices(userId: string): void {
    const user = this.usersInGroups[this.selectedGroupId!]?.find(
      (u) => u.id === userId || u.keycloakId === userId
    );
    if (user && user.keycloakId) {
      const dialogRef = this.dialog.open(UserInvoicesComponent, {
        width: '600px',
        data: { userId: user.keycloakId },
      });

      dialogRef.afterClosed().subscribe((result) => {
        // Handle dialog close if necessary
      });
    } else {
      console.error('User not found or Keycloak ID missing.');
      this.snackBar.open('User not found or Keycloak ID missing.', 'Close', { duration: 3000 });
    }
  }

  editUser(user: UserDTO): void {
    const dialogRef = this.dialog.open(EditUserComponent, {
      width: '400px',
      data: { user },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'success') {
        this.loadUsersInGroups();
        this.snackBar.open('User updated successfully.', 'Close', { duration: 3000 });
      }
    });
  }

  deleteUser(user: UserDTO): void {
    const dialogData: DeleteConfirmationDialogData = {
      title: 'Confirm Deletion',
      message: `Are you sure you want to delete user "${user.userName}"?`,
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
    };

    const dialogRef = this.dialog.open(DeleteConfirmationDialogComponent, {
      width: '350px',
      data: dialogData,
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed && user.id) {
        this.userService.deleteUser(user.id).subscribe(
          () => {
            console.log('User deleted successfully.');
            this.snackBar.open('User deleted successfully.', 'Close', { duration: 3000 });
            this.loadUsersInGroups();
          },
          (error) => {
            console.error('Error deleting user:', error);
            this.snackBar.open('Failed to delete user.', 'Close', { duration: 3000 });
          }
        );
      } else {
        console.log('User deletion canceled.');
      }
    });
  }
}
