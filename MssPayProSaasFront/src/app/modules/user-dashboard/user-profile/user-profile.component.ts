import { Component, OnInit } from '@angular/core';
import { UserDTO } from '../../user/user.dto';
import { GroupDto } from '../../group/group.dto';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { UserService } from '../../user/user.service';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  user: UserDTO | null = null;
  userGroups: GroupDto[] = [];
displayedUserColumns: any;
userProfile: any;
accessibleApis: any;

  constructor(private keycloakService: KeycloakService, private userService: UserService) {}

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
      },
      error => {
        console.error('Error loading user details', error);
        if (error.status === 401) {
          // Handle unauthorized error, e.g., by refreshing the token
        }
      }
    );
  }
}
