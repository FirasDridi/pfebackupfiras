import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { UserService } from '../../user/user.service';
import { UserDTO } from '../../user/user.dto';
import { GroupDto } from '../../group/group.dto';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  userProfileForm!: FormGroup;
  isEditMode = false;
  user: UserDTO | null = null;
  userGroups: GroupDto[] = [];

  constructor(
    private keycloakService: KeycloakService,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    const userId = this.keycloakService.getUserId();
    if (userId) {
      this.loadUserDetails(userId);
    } else {
      console.error('User ID is not available');
    }
  }

  loadUserDetails(userId: string): void {
    this.userService.getUserDetails(userId).subscribe(
      response => {
        this.user = response.user || null;
        this.userGroups = response.groups || [];
        this.userProfileForm = this.formBuilder.group({
          username: [{ value: this.user?.username, disabled: true }],
          firstName: [this.user?.firstName, Validators.required],
          lastName: [this.user?.lastName, Validators.required],
          email: [this.user?.email, [Validators.required, Validators.email]]
        });
      },
      error => {
        console.error('Error loading user details', error);
      }
    );
  }

  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;
    if (!this.isEditMode) {
      this.ngOnInit(); // Reset form when exiting edit mode
    }
  }

  saveProfile(): void {
    if (this.userProfileForm.valid) {
      const updatedUser: UserDTO = {
        ...this.user,
        ...this.userProfileForm.value
      };
      this.userService.updateUser(updatedUser).subscribe(
        () => {
          this.snackBar.open('Profile updated successfully', 'Close', { duration: 3000 });
          this.isEditMode = false;
        },
        () => {
          this.snackBar.open('Failed to update profile', 'Close', { duration: 3000 });
        }
      );
    }
  }

  resetPassword(): void {
    const accountUrl = this.keycloakService.getKeycloakInstance().createAccountUrl();
    window.open(accountUrl, '_blank');
  }
}
