import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { UserService } from '../user.service';
import { EditUserComponent } from '../edit-user/edit-user.component';
import { UserDTO } from '../user.dto';
import { UserInvoicesComponent } from '../../facture/user-invoices/user-invoices.component';

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
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe(
      (users) => {
        this.users = users;
        this.dataSource.data = this.users;
        this.totalUsers = users.length;
      },
      (error) => {
        console.error('Error fetching users:', error);
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

  viewUserInvoices(userId: string): void { // Updated method
    const dialogRef = this.dialog.open(UserInvoicesComponent, {
      width: '600px',
      data: { userId: userId } // Pass the correct user ID
    });

    dialogRef.afterClosed().subscribe(result => {
      // Handle dialog close if necessary
    });
  }
}
