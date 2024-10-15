import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { UserService } from '../../user/user.service';
import { UserDTO } from '../../user/user.dto';
import { GroupDto } from '../../group/group.dto';
import { EditProfileDialogComponent } from './editprofiledialog/edit-profile-dialog/edit-profile-dialog.component';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
})
export class UserProfileComponent implements OnInit {
  user: UserDTO | null = null;
  userGroups: GroupDto[] = [];

  constructor(
    private keycloakService: KeycloakService,
    private snackBar: MatSnackBar,
    private userService: UserService,
    private dialog: MatDialog,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const userId = this.keycloakService.getUserId();
    if (userId) {
      this.loadUserDetails(userId);
    } else {
      console.error('User ID is not available');
      this.snackBar.open('User ID is not available', 'Close', {
        duration: 3000,
      });
    }
  }

  loadUserDetails(userId: string): void {
    // Make a request to the provided API endpoint to fetch user and group details
    this.http.get<any>(`http://localhost:8884/admin/user/${userId}/info`).subscribe(
      (response) => {
        if (response) {
          // Set user details
          this.user = response.user;

          // Set user groups
          this.userGroups = response.groups || [];

          console.log('User:', this.user);
          console.log('User Groups:', this.userGroups);
        } else {
          console.error('User data is not available');
          this.snackBar.open('User data is not available', 'Close', {
            duration: 3000,
          });
        }
      },
      (error) => {
        console.error('Error loading user details', error);
        this.snackBar.open('Error loading user details', 'Close', {
          duration: 3000,
        });
      }
    );
  }

  openEditProfileDialog(): void {
    if (this.user) {
      const dialogRef = this.dialog.open(EditProfileDialogComponent, {
        width: '400px',
        data: {
          firstname: this.user.firstName,
          lastName: this.user.lastName,
          email: this.user.email,
        },
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          this.saveProfile(result);
        }
      });
    }
  }

  saveProfile(updatedData: any): void {
    if (this.user) {
      const userId = this.keycloakService.getUserId();
      if (!userId) {
        console.error('User ID is not available');
        this.snackBar.open('User ID is not available', 'Close', {
          duration: 3000,
        });
        return;
      }

      const updatedUser: UserDTO = {
        ...this.user,
        firstName: updatedData.firstname,
        lastName: updatedData.lastName,
        email: updatedData.email,
      };

      this.userService.updateUserById(userId, updatedUser).subscribe(
        () => {
          this.snackBar.open('Profile updated successfully', 'Close', {
            duration: 3000,
          });
          this.loadUserDetails(userId);
        },
        (error) => {
          console.error('Error updating profile:', error);
          this.snackBar.open('Error updating profile', 'Close', {
            duration: 3000,
          });
        }
      );
    }
  }
}
