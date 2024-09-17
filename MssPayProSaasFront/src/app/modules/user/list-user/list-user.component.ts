import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { UserService } from '../user.service';
import { EditUserComponent } from '../edit-user/edit-user.component';
import { UserDTO } from '../user.dto';
import { UserInvoicesComponent } from '../../facture/user-invoices/user-invoices.component';
import { FactureService } from '../../facture/facture.service'; // Import FactureService

@Component({
  selector: 'app-list-user',
  templateUrl: './list-user.component.html',
  styleUrls: ['./list-user.component.css']
})
export class ListUserComponent implements OnInit {
  users: UserDTO[] = [];
  displayedColumns: string[] = ['userName', 'email', 'firstname', 'lastName', 'actions'];
  dataSource = new MatTableDataSource<UserDTO>(this.users);
  totalUsers: number | undefined;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private userService: UserService,
    private factureService: FactureService, // Inject FactureService
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
    this.userService.getAllUsers().subscribe(
      (users) => {
        this.users = users.map(user => ({
          ...user,
          isSuperuser: this.checkIfSuperuser(user) // Add this line to identify superusers
        }));
        this.dataSource.data = this.users;
        this.totalUsers = users.length;
      },
      (error) => {
        console.error('Error fetching users:', error);
      }
    );
  }

  checkIfSuperuser(user: UserDTO): boolean {
    // Assuming that a certain role or flag in the user object indicates a superuser
    return user.roles && user.roles.includes('superuser');  // Adjust according to your actual data structure
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

  deleteUser(id: string): void {
    this.userService.deleteUser(id).subscribe(
      () => {
        this.loadUsers();
      },
      (error) => {
        console.error('Error deleting user:', error);
      }
    );
  }

  viewUserInvoices(userId: string): void {
    const user = this.users.find(u => u.id === userId || u.keycloakId === userId); // Check against both just in case
    if (user && user.keycloakId) { // Use 'keycloakId'
      const dialogRef = this.dialog.open(UserInvoicesComponent, {
        width: '600px',
        data: { userId: user.keycloakId } // Pass the Keycloak ID to the dialog
      });

      dialogRef.afterClosed().subscribe(result => {
        // Handle dialog close if necessary
      });
    } else {
      console.error('User not found or Keycloak ID missing.');
    }
  }
}
