// File Path: src/app/modules/group/group-users/group-users.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { GroupService } from '../group-service.service';
import { UserService } from '../../user/user.service';
import { UserGroupDTO } from '../group/UserGroupDTO';
import { MatDialog } from '@angular/material/dialog';
import { AddUserComponent } from '../../user/add-user/add-user.component';
import { forkJoin, of } from 'rxjs';
import { catchError, map, mergeMap, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-group-users',
  templateUrl: './group-users.component.html',
  styleUrls: ['./group-users.component.css'],
})
export class GroupUsersComponent implements OnInit {
  groupId: string = '';
  groupName: string = '';
  users: UserGroupDTO[] = [];
  isLoading: boolean = false; // Flag for loading spinner

  constructor(
    private route: ActivatedRoute,
    private groupService: GroupService,
    private dialog: MatDialog,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.groupId = this.route.snapshot.paramMap.get('groupId') || '';
    this.loadGroupUsers();
  }

  loadGroupUsers(): void {
    this.isLoading = true; // Show spinner

    this.groupService.getGroupUsers(this.groupId).pipe(
      mergeMap((users: UserGroupDTO[]) => {
        if (!users || users.length === 0) {
          return of([]); // Return empty array if no users
        }

        // Create an array of observables to fetch superUser details
        const userDetailObservables = users.map(user => {
          if (user.keycloakId) {
            return this.userService.getUserDetailsFromKeycloak(user.keycloakId).pipe(
              catchError(err => {
                console.error(`Error fetching details for user ${user.id}:`, err);
                // Return null if there's an error
                return of(null);
              })
            );
          } else {
            console.warn(`User ${user.id} does not have a keycloakId.`);
            return of(null);
          }
        });

        // Use forkJoin to wait for all requests to complete
        return forkJoin(userDetailObservables).pipe(
          map(userDetails => {
            // Merge isSuperuser flag into the original users array
            return users.map((user, index) => {
              const details = userDetails[index];
              return {
                ...user,
                isSuperuser: details?.roles?.includes('superuser') ?? false // Ensure isSuperuser is a boolean
              };
            });
          })
        );
      }),
      finalize(() => {
        this.isLoading = false; // Hide spinner
      })
    ).subscribe(
      (usersWithSuperUser) => {
        this.groupName = usersWithSuperUser.length > 0 ? usersWithSuperUser[0].groupName || 'Unknown' : 'Unknown';
        this.users = usersWithSuperUser;
      },
      (error) => {
        console.error('Error fetching group users:', error);
        // Optionally show an error notification to the user here
      }
    );
  }

  openAddUserDialog(): void {
    const dialogRef = this.dialog.open(AddUserComponent, {
      width: '400px',
      data: { groupId: this.groupId, groupName: this.groupName },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadGroupUsers();
      }
    });
  }

  addUsersToGroup(users: UserGroupDTO[]): void {
    this.userService.addUsersToGroup(this.groupName, users).subscribe(
      (response: any) => {
        console.log(response.message);
        this.loadGroupUsers();
      },
      (error: any) => {
        console.error('Error adding users to group:', error);
      }
    );
  }
}
