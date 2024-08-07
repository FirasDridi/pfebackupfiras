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

  constructor(
    private groupService: GroupService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private keycloakService: KeycloakService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.keycloakService.hasRole('admin');
    this.loadGroups();
  }

  loadGroups(): void {
    this.groupService.getAllGroups().subscribe(
      (groups) => {
        this.groups = groups;
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

  deleteGroup(groupId: string): void {
    this.groupService.deleteGroup(groupId).subscribe(
      () => {
        console.log('Group deleted successfully.');
        this.loadGroups();
      },
      (error) => {
        console.error('Error deleting group:', error);
        this.snackBar.open('Failed to delete group', 'Close', {
          duration: 3000,
        });
      }
    );
  }

  toggleToken(groupId: string): void {
    this.groupService.toggleAccessToken(groupId).subscribe({
      next: (group) => {
        console.log('Token toggled for group:', group);
        this.snackBar.open('Token toggled successfully', 'Close', {
          duration: 3000,
        });
        this.loadGroups();
      },
      error: (err) => {
        console.error('Error toggling token:', err);
        this.snackBar.open('Failed to toggle token', 'Close', {
          duration: 3000,
        });
      },
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
      data: { groupId: groupId } // Pass the correct group ID
    });

    dialogRef.afterClosed().subscribe(result => {
      // Handle dialog close if necessary
    });
  }
}
