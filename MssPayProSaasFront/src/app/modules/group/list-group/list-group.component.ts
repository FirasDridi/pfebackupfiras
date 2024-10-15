import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { GroupService } from '../group-service.service';
import { AddUserComponent } from '../../user/add-user/add-user.component';
import { EditGroupComponent } from '../edit-group/edit-group.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { HttpClient } from '@angular/common/http';
import { GroupInvoicesComponent } from '../../facture/group-invoices/group-invoices.component';
import { FactureService } from '../../facture/facture.service';
import { DeleteConfirmationDialogComponent, DeleteConfirmationDialogData } from '../../api-service/add-api/delete-confirmation-dialog/delete-confirmation-dialog.component';

@Component({
  selector: 'app-list-group',
  templateUrl: './list-group.component.html',
  styleUrls: ['./list-group.component.css'],
})
export class ListGroupComponent implements OnInit {
  groups: any[] = [];
  groupServices: any[] = [];
  displayedColumns: string[] = ['name', 'actions'];
  isAdmin = false;
  totalGroups = 0;

  constructor(
    private groupService: GroupService,
    private factureService: FactureService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private keycloakService: KeycloakService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.keycloakService.hasRole('admin');
    this.generateInvoicesOnPageLoad();
    this.loadGroups();
  }

  generateInvoicesOnPageLoad(): void {
    this.factureService.generateInvoices().subscribe({
      next: () => {
        console.log('Invoices generated successfully on page load');
      },
      error: (err) => {
        console.error('Error generating invoices on page load:', err);
        this.snackBar.open('Failed to generate invoices on page load', 'Close', {
          duration: 3000,
        });
      },
    });
  }

  loadGroups(): void {
    this.groupService.getAllGroups().subscribe(
      (groups) => {
        this.groups = groups;
        this.totalGroups = groups.length;
      },
      (error) => {
        console.error('Error fetching groups:', error);
        this.snackBar.open('Failed to load groups', 'Close', {
          duration: 3000,
        });
      }
    );
  }

  viewUsers(groupId: string): void {
    this.router.navigate(['/admins/group', groupId, 'users']);
  }

  addUsers(groupId: string, groupName: string): void {
    const dialogRef = this.dialog.open(AddUserComponent, {
      width: '400px',
      data: { groupName: groupName },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadGroups();
      }
    });
  }

  addSuperUser(groupId: string, groupName: string): void {
    const dialogRef = this.dialog.open(AddUserComponent, {
      width: '400px',
      data: { groupName: groupName, role: 'superuser' },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadGroups();
      }
    });
  }

  editGroup(group: any): void {
    const dialogRef = this.dialog.open(EditGroupComponent, {
      width: '500px',
      data: { group: group },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadGroups();
      }
    });
  }

  /**
   * Opens a confirmation dialog before deleting a group.
   * @param groupId The ID of the group to delete.
   */
  deleteGroup(groupId: string): void {
    const dialogData: DeleteConfirmationDialogData = {
      title: 'Confirm Deletion',
      message: 'Are you sure you want to delete this client?',
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
    };

    const dialogRef = this.dialog.open(DeleteConfirmationDialogComponent, {
      width: '350px',
      data: dialogData,
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        // Proceed with deletion
        this.groupService.deleteGroup(groupId).subscribe(
          () => {
            console.log('Group deleted successfully.');
            this.snackBar.open('Group deleted successfully.', 'Close', {
              duration: 3000,
            });
            this.loadGroups();
          },
          (error) => {
            console.error('Error deleting group:', error);
            this.snackBar.open('Failed to delete group.', 'Close', {
              duration: 3000,
            });
          }
        );
      } else {
        // Deletion was canceled
        console.log('Group deletion canceled.');
      }
    });
  }

  deleteAccessTokens(groupId: string): void {
    const dialogData: DeleteConfirmationDialogData = {
      title: 'Confirm Token Deletion',
      message: 'Are you sure you want to delete all access tokens for this group?',
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
    };

    const dialogRef = this.dialog.open(DeleteConfirmationDialogComponent, {
      width: '350px',
      data: dialogData,
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.groupService.deleteAllAccessTokens(groupId).subscribe(
          (response) => {
            console.log(response.message);
            this.snackBar.open(response.message, 'Close', {
              duration: 3000,
            });
            this.loadGroups();
          },
          (error) => {
            console.error('Error deleting access tokens:', error);
            this.snackBar.open('Failed to delete access tokens.', 'Close', {
              duration: 3000,
            });
          }
        );
      } else {
        // Deletion was canceled
        console.log('Access token deletion canceled.');
      }
    });
  }


  loadGroupServices(groupId: string): void {
    this.http
      .get<any[]>(`http://localhost:8884/admin/groups/${groupId}/services`)
      .subscribe(
        (services: any[]) => {
          this.groupServices = services;
        },
        (error) => {
          console.error('Error fetching group services:', error);
        }
      );
  }

  viewGroupInvoices(groupId: string): void {
    console.log('Viewing invoices for group ID:', groupId);
    const dialogRef = this.dialog.open(GroupInvoicesComponent, {
      width: '600px',
      data: { groupId: groupId },
    });

    dialogRef.afterClosed().subscribe((result) => {
      // Handle dialog close if necessary
    });
  }
}
