import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { GroupService } from '../group-service.service';
import { UserService } from '../../user/user.service'; // Import UserService
import { UserGroupDTO } from '../group/UserGroupDTO';
import { MatDialog } from '@angular/material/dialog';
import { AddUserComponent } from '../../user/add-user/add-user.component';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-group-users',
  templateUrl: './group-users.component.html',
  styleUrls: ['./group-users.component.css'],
})
export class GroupUsersComponent implements OnInit {
  groupId: string = '';
  groupName: string = '';
  users: UserGroupDTO[] = [];

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
    this.groupService.getGroupUsers(this.groupId).subscribe(
      (users: UserGroupDTO[]) => {
        this.groupName =
          users.length > 0 ? users[0].groupName || 'Unknown' : 'Unknown';
        this.users = users;
      },
      (error: any) => {
        console.error('Error fetching group users:', error);
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
