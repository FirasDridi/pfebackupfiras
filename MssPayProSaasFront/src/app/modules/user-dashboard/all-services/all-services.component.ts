// all-services.component.ts
import {
  Component,
  OnInit,
  ViewChild,
  AfterViewInit,
  ChangeDetectorRef,
} from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { merge, of, Observable } from 'rxjs';
import { switchMap, catchError, map } from 'rxjs/operators';
import { ServiceDto } from '../../api-service/ServiceDto';
import { HttpClient } from '@angular/common/http';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { UserService } from '../../user/user.service';
import { LoadingDialogComponent } from '../loading-dialog/loading-dialog.component';
import { ServiceDetailsDialogComponent } from '../service-details-popup/service-details-popup.component';
import { GroupDto } from '../../group/group.dto';

@Component({
  selector: 'app-all-services',
  templateUrl: './all-services.component.html',
  styleUrls: ['./all-services.component.css'],
})
export class AllServicesComponent implements OnInit, AfterViewInit {
  searchQuery: string = '';
  dataSource: MatTableDataSource<ServiceDto> = new MatTableDataSource<ServiceDto>();
  displayedColumns: string[] = [
    'date',
    'name',
    'description',
    'status',
    'endpoint',
    'subscriptionStatus',
    'details',
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  keycloakId: string | null = null;
  userId: string | null = null;
  groupId: number | null = null;
  isSuperUser: boolean = false;

  constructor(
    private http: HttpClient,
    private keycloakService: KeycloakService,
    private userService: UserService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('Initializing AllServicesComponent');
    const kcId = this.keycloakService.getUserId();
    this.keycloakId = kcId !== undefined ? kcId : null;
    this.isSuperUser = this.keycloakService.hasRole('superuser');
    console.log('Keycloak ID:', this.keycloakId);
    console.log('Is SuperUser:', this.isSuperUser);
    if (this.keycloakId) {
      this.getUserAndGroupFromKeycloakId(this.keycloakId);
    } else {
      console.error('Keycloak ID is not available');
    }
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.paginator) {
        this.dataSource.paginator = this.paginator;
        this.loadServices();
      }
    });
  }

  getUserAndGroupFromKeycloakId(keycloakId: string): void {
    console.log('Fetching user and group info for Keycloak ID:', keycloakId);
    this.http
      .get<any>(`http://localhost:8884/admin/getUserByKeycloakId/${keycloakId}`)
      .subscribe(
        (response) => {
          if (response && response.id) {
            this.userId = response.id;
            console.log('User ID fetched:', this.userId);
            this.getGroupFromUserInfo(keycloakId);
          } else {
            console.error('User ID not found for keycloak ID:', keycloakId);
          }
        },
        (error) => {
          console.error('Error fetching user ID:', error);
        }
      );
  }

  getGroupFromUserInfo(keycloakId: string): void {
    this.http
      .get<any>(`http://localhost:8884/admin/user/${keycloakId}/info`)
      .subscribe(
        (response) => {
          if (response.groups && response.groups.length > 0) {
            this.groupId = response.groups[0].groupId;
            console.log('Group ID fetched:', this.groupId);
            this.loadServices();
          } else {
            console.error('No group found for the user.');
          }
        },
        (error) => {
          console.error('Error fetching group info:', error);
        }
      );
  }

  loadServices(searchData?: any): void {
    console.log('Loading services...');
    merge(this.paginator.page, of({}))
      .pipe(
        switchMap(() => {
          const pageIndex: number = this.paginator.pageIndex;
          const pageSize: number = this.paginator.pageSize;
          console.log(`Fetching services list - Page Index: ${pageIndex}, Page Size: ${pageSize}`);
          return this.http.get<any>(
            `http://localhost:8081/service/api/services/list?page=${pageIndex}&size=${pageSize}`
          );
        })
      )
      .subscribe(
        (response: any) => {
          const servicesArray = response.content || [];
          servicesArray.forEach((service: ServiceDto) => {
            this.checkSubscriptionStatus(service);
          });
          this.dataSource.data = servicesArray;
          console.log('Services loaded:', this.dataSource.data);
          this.paginator.length = response.totalElements;
          this.cdr.detectChanges();
        },
        (error) => {
          console.error('Error loading services:', error);
          this.dataSource.data = [];
          this.cdr.detectChanges();
        }
      );
  }

  checkSubscriptionStatus(service: ServiceDto): void {
    if (this.groupId !== null) {
      console.log('Checking subscription status for service ID:', service.id);
      this.http
        .get<any>(
          `http://localhost:8884/admin/subscriptions/status/${service.id}`,
          {
            params: { groupId: this.groupId.toString() },
          }
        )
        .subscribe(
          (response) => {
            service.subscriptionStatus = response.status;
            console.log(`Subscription status for service ${service.id}:`, service.subscriptionStatus);
            this.cdr.detectChanges(); // Ensure the view updates with the latest status
          },
          (error) => {
            console.error('Error fetching subscription status:', error);
          }
        );
    }
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    console.log('Applying filter with value:', filterValue);
    this.dataSource.filter = filterValue.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  subscribeService(service: ServiceDto): void {
    if (this.keycloakId === null || this.userId === null || this.groupId === null) {
      console.error('Keycloak ID, User ID, or Group ID is not available');
      return;
    }

    if (!service.status) {
      console.error('Cannot subscribe to an inactive service');
      this.dialog.open(ServiceDetailsDialogComponent, {
        data: { message: 'This service is currently inactive and cannot be subscribed to.' },
      });
      return;
    }

    console.log('Subscribing to service:', service);
    const loadingDialogRef = this.dialog.open(LoadingDialogComponent);

    // Check if the subscription status is not pending before making a request
    if (service.subscriptionStatus === 'PENDING') {
      loadingDialogRef.close();
      console.error('Subscription request is still pending. Cannot subscribe again.');
      this.dialog.open(ServiceDetailsDialogComponent, {
        data: { message: 'Subscription request is still pending. Please wait for admin approval or rejection.' },
      });
      return;
    }

    console.log('Sending subscription request for service ID:', service.id);
    this.http
      .post<any>(
        `http://localhost:8884/admin/subscriptions/request/${service.id}`,
        null,
        {
          params: { groupId: this.groupId.toString(), userId: this.userId }
        }
      )
      .subscribe(
        (response) => {
          loadingDialogRef.close();
          console.log('Subscription request response:', response);
          this.dialog.open(ServiceDetailsDialogComponent, {
            data: { message: 'Subscription request submitted successfully. Current status: ' + response.status },
          });
          this.checkSubscriptionStatus(service); // Update the subscription status
        },
        (error) => {
          console.error('Error subscribing to service:', error);
          loadingDialogRef.close();
          this.dialog.open(ServiceDetailsDialogComponent, {
            data: { message: 'Error subscribing to service' },
          });
        }
      );
  }

  viewDetails(service: ServiceDto): void {
    if (this.keycloakId === null || this.groupId === null) {
      console.error('Keycloak ID or Group ID is not available');
      return;
    }

    console.log('Viewing details for service:', service);
    const loadingDialogRef = this.dialog.open(LoadingDialogComponent);

    console.log('Sending use service request for service ID:', service.id);
    this.http
      .post<any>(
        `http://localhost:8081/service/api/services/useService`,
        null,
        {
          params: {
            groupId: this.groupId.toString(),
            userId: this.userId!,
            serviceName: service.name!,
            serviceId: service.id!,
          },
        }
      )
      .pipe(
        switchMap((response) => {
          console.log('Service used successfully:', response);
          console.log('Fetching logs...');
          return this.http.get<string[]>(
            `http://localhost:8081/service/api/logs`
          );
        }),
        catchError((error) => {
          console.error('Error performing service check:', error);
          // Proceed to fetch logs even if useService fails
          console.log('Attempting to fetch logs despite service check failure...');
          return this.http
            .get<string[]>(`http://localhost:8081/service/api/logs`)
            .pipe(
              catchError((logError) => {
                console.error('Error fetching logs:', logError);
                loadingDialogRef.close();
                this.dialog.open(ServiceDetailsDialogComponent, {
                  data: {
                    message: 'Error performing check and fetching logs',
                  },
                });
                throw logError;
              })
            );
        })
      )
      .subscribe(
        (logs) => {
          loadingDialogRef.close();
          console.log('Fetched logs:', logs);
          this.dialog.open(ServiceDetailsDialogComponent, {
            data: { message: logs.join('\n') },
          });
        },
        (error) => {
          console.error('Error fetching logs:', error);
          loadingDialogRef.close();
          this.dialog.open(ServiceDetailsDialogComponent, {
            data: { message: 'Error fetching logs' },
          });
        }
      );
  }
}
