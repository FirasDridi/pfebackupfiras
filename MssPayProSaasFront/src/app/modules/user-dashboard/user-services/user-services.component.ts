import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { GroupDto } from '../../group/group.dto';
import { ServiceDetailsDto } from '../../api-service/ServiceDetailsDto';
import { UserService } from '../../user/user.service';
import { KeycloakService } from '../../keycloak/keycloak.service';

@Component({
  selector: 'app-user-services',
  templateUrl: './user-services.component.html',
  styleUrls: ['./user-services.component.css']
})
export class UserServicesComponent implements OnInit {
applyFilter($event: KeyboardEvent) {
throw new Error('Method not implemented.');
}
  userGroups: GroupDto[] = [];
  serviceIds: string[] = [];
  services: ServiceDetailsDto[] = [];
  dataSource: MatTableDataSource<ServiceDetailsDto> = new MatTableDataSource<ServiceDetailsDto>();
  displayedColumns: string[] = ['name', 'description', 'status'];

  constructor(
    private userService: UserService,
    private keycloakService: KeycloakService
  ) {}

  ngOnInit(): void {
    const userId = this.keycloakService.getUserId();
    this.loadUserDetails(userId);
  }

  loadUserDetails(userId: string | undefined): void {
    if (!userId) {
      console.error('User ID is undefined');
      return;
    }

    this.userService.getUserDetails(userId).subscribe(
      (response) => {
        this.userGroups = response.groups;
        this.loadServicesForAllGroups();
      },
      (error) => {
        console.error('Error loading user details', error);
      }
    );
  }

  loadServicesForAllGroups(): void {
    if (this.userGroups.length > 0) {
      this.userGroups.forEach((group) => {
        this.loadServiceIds(group.id);
      });
    }
  }

  loadServiceIds(groupId: number | undefined): void {
    if (!groupId) {
      console.error('Group ID is undefined');
      return;
    }

    this.userService.getServiceIdsByGroupId(groupId).subscribe(
      (response) => {
        this.serviceIds = response;
        this.loadServices(this.serviceIds);
      },
      (error) => {
        console.error('Error loading service IDs', error);
      }
    );
  }

  loadServices(ids: string[]): void {
    if (!ids || ids.length === 0) {
      console.error('Service IDs are undefined or empty');
      return;
    }

    this.userService.getServicesByIds(ids).subscribe(
      (response) => {
        this.services = response;
        this.dataSource.data = this.services;
      },
      (error) => {
        console.error('Error loading services', error);
      }
    );
  }
}
