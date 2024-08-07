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
    'groups',
    'subscriptionStatus',
    'details',
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  keycloakId: string | null = null;
  userId: number | null = null;
  isSuperUser: boolean = false;

  constructor(
    private http: HttpClient,
    private keycloakService: KeycloakService,
    private userService: UserService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const kcId = this.keycloakService.getUserId();
    this.keycloakId = kcId !== undefined ? kcId : null;
    this.isSuperUser = this.keycloakService.hasRole('superuser');
    if (this.keycloakId) {
      this.getUserIdFromKeycloakId(this.keycloakId);
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

  getUserIdFromKeycloakId(keycloakId: string): void {
    this.http
      .get<any>(`http://localhost:8884/admin/getUserByKeycloakId/${keycloakId}`)
      .subscribe(
        (response) => {
          if (response && response.id) {
            this.userId = response.id;
            this.loadServices(); // Ensure services are loaded after getting user ID
          } else {
            console.error('User ID not found for keycloak ID:', keycloakId);
          }
        },
        (error) => {
          console.error('Error fetching user ID:', error);
        }
      );
  }

  getGroupIdFromUserDetails(userId: string): Observable<number> {
    return this.userService.getUserDetails(userId).pipe(
      map((response) => {
        const userGroups: GroupDto[] = response.groups || [];
        if (userGroups.length > 0) {
          return userGroups[0].groupId || 0; // Using groupId from the response
        } else {
          throw new Error('No groups found for user ID');
        }
      }),
      catchError((error) => {
        console.error('Error fetching group ID:', error);
        return of(0); // Provide a default value or handle error
      })
    );
  }

  loadServices(searchData?: any): void {
    merge(this.paginator.page, of({}))
      .pipe(
        switchMap(() => {
          const pageIndex: number = this.paginator.pageIndex;
          const pageSize: number = this.paginator.pageSize;
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
    if (this.userId !== null) {
      this.http
        .get<any>(
          `http://localhost:8884/admin/subscriptions/status/${service.id}`,
          {
            params: { groupId: this.userId.toString() },
          }
        )
        .subscribe(
          (response) => {
            service.subscriptionStatus = response.status;
          },
          (error) => {
            console.error('Error fetching subscription status:', error);
          }
        );
    }
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  subscribeService(service: ServiceDto): void {
    if (this.keycloakId === null) {
      console.error('Keycloak ID is not available');
      return;
    }

    const loadingDialogRef = this.dialog.open(LoadingDialogComponent);

    this.getGroupIdFromUserDetails(this.keycloakId).subscribe((groupId) => {
      if (groupId === 0) {
        loadingDialogRef.close();
        this.dialog.open(ServiceDetailsDialogComponent, {
          data: { message: 'Error performing check: No group found' },
        });
        return;
      }

      this.http
        .post<any>(
          `http://localhost:8884/admin/subscriptions/request/${service.id}`,
          null,
          {
            params: { groupId: groupId.toString(), userId: this.userId!.toString() }
          }
        )
        .subscribe(
          (response) => {
            loadingDialogRef.close();
            this.dialog.open(ServiceDetailsDialogComponent, {
              data: { message: 'Subscription request submitted successfully' },
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
    });
  }

  viewDetails(service: ServiceDto): void {
    if (this.keycloakId === null) {
      console.error('Keycloak ID is not available');
      return;
    }

    const loadingDialogRef = this.dialog.open(LoadingDialogComponent);

    this.getGroupIdFromUserDetails(this.keycloakId).subscribe((groupId) => {
      if (groupId === 0) {
        loadingDialogRef.close();
        this.dialog.open(ServiceDetailsDialogComponent, {
          data: { message: 'Error performing check: No group found' },
        });
        return;
      }

      this.http
        .post<any>(
          `http://localhost:8081/service/api/services/useService`,
          null,
          {
            params: {
              groupId: groupId.toString(),
              userId: this.userId!.toString(),
              serviceName: service.name!,
              serviceId: service.id!,
            },
          }
        )
        .pipe(
          switchMap((response) => {
            console.log('Service used successfully:', response);
            return this.http.get<string[]>(
              `http://localhost:8081/service/api/logs`
            );
          }),
          catchError((error) => {
            console.error('Error performing service check:', error);
            // Proceed to fetch logs even if useService fails
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
    });
  }
}
