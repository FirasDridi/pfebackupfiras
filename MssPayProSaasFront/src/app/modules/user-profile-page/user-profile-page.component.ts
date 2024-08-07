import { Component, OnInit } from '@angular/core';
import { KeycloakService } from '../keycloak/keycloak.service';
import { GroupService } from '../group/group-service.service';

@Component({
  selector: 'app-user-profile-page',
  templateUrl: './user-profile-page.component.html',
  styleUrls: ['./user-profile-page.component.css']
})
export class UserProfilePageComponent implements OnInit {
  userProfile: any;
  accessibleApis: any[] = [];
  userGroups: any[] = [];

  constructor(public keycloakService: KeycloakService, private groupService: GroupService) {}

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.keycloakService.getUserProfile().then(profile => {
      this.userProfile = profile;
      this.loadUserGroups(profile.id);
    });
  }

  loadUserGroups(userId: string): void {
    this.groupService.getGroupUsers(userId).subscribe((groups: any[]) => {
      this.userGroups = groups;
    });
  }
}
