// user-profile.component.ts

import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { UserService } from '../../user/user.service';
import { UserDTO } from '../../user/user.dto';
import { GroupDto } from '../../group/group.dto';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
})
export class UserProfileComponent implements OnInit {
  user: UserDTO | null = null;
  userGroups: GroupDto[] = [];
  editProfileForm!: FormGroup;

  constructor(
    private keycloakService: KeycloakService,
    private snackBar: MatSnackBar,
    private userService: UserService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    // Initialize the form with empty/default values
    this.editProfileForm = this.fb.group({
      firstname: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.minLength(6)]], // Password is optional
    });

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

  /**
   * Loads user details from the backend.
   * @param userId The Keycloak ID of the user.
   */
  loadUserDetails(userId: string): void {
    this.userService.getUserDetailsFromKeycloak(userId).subscribe(
      (userDTO) => {
        if (userDTO) {
          this.user = userDTO;
          this.userGroups = userDTO.groups || [];

          console.log('User details loaded:', this.user);

          // Update the form with user data
          this.editProfileForm.patchValue({
            firstname: this.user.firstname,
            lastName: this.user.lastName,
            email: this.user.email,
          });
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

  /**
   * Saves the updated profile.
   */
  saveProfile(): void {
    if (this.editProfileForm.valid && this.user) {
      const userId = this.keycloakService.getUserId();
      if (!userId) {
        console.error('User ID is not available');
        this.snackBar.open('User ID is not available', 'Close', {
          duration: 3000,
        });
        return;
      }

      const updatedUser: UserDTO = {
        id: this.user.id, // Database ID
        userName: this.user.userName,
        firstname: this.editProfileForm.value.firstname,
        lastName: this.editProfileForm.value.lastName,
        email: this.editProfileForm.value.email,
        password: this.editProfileForm.value.password,
        superUser: this.user.superUser, // Preserve existing superUser status
        roles: this.user.roles, // Preserve existing roles
        keycloakId: this.user.keycloakId, // Preserve Keycloak ID
      };

      console.log('Updating user with:', updatedUser);

      this.userService.updateUserById(userId, updatedUser).subscribe(
        (response) => {
          this.snackBar.open('Profile updated successfully', 'Close', {
            duration: 3000,
          });
          this.loadUserDetails(userId); // Reload user details
        },
        (error) => {
          console.error('Error updating profile:', error);
          this.snackBar.open('Error updating profile', 'Close', {
            duration: 3000,
          });
        }
      );
    } else {
      this.snackBar.open('Please fill in all required fields', 'Close', {
        duration: 3000,
      });
    }
  }
}
