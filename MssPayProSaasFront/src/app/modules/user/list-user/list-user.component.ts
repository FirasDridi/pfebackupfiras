// File Path: src/app/modules/user/list-user/list-user.component.ts

import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { UserService } from '../user.service';
import { EditUserComponent } from '../edit-user/edit-user.component';
import { UserDTO } from '../user.dto';
import { UserInvoicesComponent } from '../../facture/user-invoices/user-invoices.component';
import { FactureService } from '../../facture/facture.service';
import { forkJoin, of } from 'rxjs';
import { catchError, map, finalize, mergeMap } from 'rxjs/operators';
import { DeleteConfirmationDialogComponent, DeleteConfirmationDialogData } from '../../api-service/add-api/delete-confirmation-dialog/delete-confirmation-dialog.component';

@Component({
  selector: 'app-list-user',
  templateUrl: './list-user.component.html',
  styleUrls: ['./list-user.component.css']
})
export class ListUserComponent implements OnInit {
  users: UserDTO[] = [];
  displayedColumns: string[] = ['userName', 'email', 'firstname', 'lastName', 'groups', 'actions']; // Added 'groups'
  dataSource = new MatTableDataSource<UserDTO>(this.users);
  totalUsers: number | undefined;
  isLoading: boolean = false; // Flag for loading spinner

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private userService: UserService,
    private factureService: FactureService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.generateInvoicesOnPageLoad(); // Generate invoices when the page loads
    this.loadUsers();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  generateInvoicesOnPageLoad(): void {
    this.factureService.generateInvoices().subscribe({
      next: () => {
        console.log('Invoices generated successfully on page load');
      },
      error: (error) => {
        console.error('Failed to generate invoices on page load:', error);
        // Optionally show a notification to the user here
      }
    });
  }

  loadUsers(): void {
    this.isLoading = true; // Show spinner
    this.userService.getAllDbUsers().pipe(
      mergeMap(users => {
        if (!users || users.length === 0) {
          return of([]); // Return empty array if no users
        }

        // Create an array of observables to fetch superUser details and groups
        const userDetailObservables = users.map(user => {
          const userDetailsObservable = user.keycloakId ? this.userService.getUserDetailsFromKeycloak(user.keycloakId).pipe(
            catchError(err => {
              console.error(`Error fetching details for user ${user.id}:`, err);
              // Return null if there's an error
              return of(null);
            })
          ) : of(null);

          // Fetch groups for each user using user.id
          const userGroupsObservable = this.userService.getUserGroups(user.id).pipe(
            catchError(err => {
              console.error(`Error fetching groups for user ${user.id}:`, err);
              // Return empty array if there's an error
              return of([]);
            })
          );

          // Combine observables for user details and groups
          return forkJoin({
            userDetails: userDetailsObservable,
            userGroups: userGroupsObservable
          }).pipe(
            map(({ userDetails, userGroups }) => {
              return {
                ...user,
                isSuperuser: userDetails?.superUser ?? false, // Ensure isSuperuser is a boolean
                groups: userGroups // Add groups to the user object
              };
            })
          );
        });

        // Use forkJoin to wait for all requests to complete
        return forkJoin(userDetailObservables);
      }),
      finalize(() => {
        this.isLoading = false; // Hide spinner
      })
    ).subscribe(
      (usersWithDetails) => {
        this.users = usersWithDetails;
        this.dataSource.data = this.users;
        this.totalUsers = usersWithDetails.length;
        console.log('Mapped Users:', this.users); // For debugging
      },
      (error) => {
        console.error('Error fetching users:', error);
        this.isLoading = false; // Hide spinner on error
      }
    );
  }

  editUser(user: UserDTO): void {
    const dialogRef = this.dialog.open(EditUserComponent, {
      width: '400px',
      data: { user }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    });
  }

  deleteUser(id: number | undefined): void {
    if (id === undefined || id === null) {
      console.error('User ID is undefined.');
      return;
    }

    const user = this.users.find(u => u.id === id);
    if (!user) {
      console.error('User not found.');
      return;
    }

    // Define the dialog data according to your DeleteConfirmationDialogData interface
    const dialogData: DeleteConfirmationDialogData = {
      title: 'Confirm Deletion',
      message: `Are you sure you want to delete user "${user.userName}"?`,
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel'
    };

    // Open the delete confirmation dialog
    const dialogRef = this.dialog.open(DeleteConfirmationDialogComponent, {
      width: '350px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        // User confirmed deletion
        this.userService.deleteUser(id).subscribe(
          () => {
            console.log(`User with ID ${id} deleted successfully.`);
            this.loadUsers();
          },
          (error) => {
            console.error('Error deleting user:', error);
            // Optionally show an error notification to the user here
          }
        );
      } else {
        // User canceled deletion
        console.log('Deletion canceled.');
      }
    });
  }

  viewUserInvoices(userId: string): void {
    if (!userId) {
      console.error('User ID is undefined.');
      return;
    }
    const user = this.users.find(u => u.id === Number(userId) || u.keycloakId === userId);
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
