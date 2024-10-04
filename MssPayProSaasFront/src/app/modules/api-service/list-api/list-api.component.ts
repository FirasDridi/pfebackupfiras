// list-api.component.ts

import { Component, OnInit, ViewChild, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { ServiceDto } from '../ServiceDto';
import { ServiceUsageService } from '../ServiceUsageService';
import { GroupService } from '../../group/group-service.service';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { MatPaginator } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { EditServiceComponent } from '../edit-service/edit-service.component';
import { switchMap, merge, of } from 'rxjs';
import { ApiDetailsComponentComponent } from '../api-details-component/api-details-component.component';
import { GroupDto } from '../../group/group.dto';
import { UserService } from '../../user/user.service';
import { AddApiComponent } from '../add-api/add-api.component';
import { FactureService } from '../../facture/facture.service';
import { DeleteConfirmationDialogComponent } from '../add-api/delete-confirmation-dialog/delete-confirmation-dialog.component';

@Component({
  selector: 'app-list-api',
  templateUrl: './list-api.component.html',
  styleUrls: ['./list-api.component.css'],
})
export class ListApiComponent implements OnInit, AfterViewInit {
  searchQuery: string = '';
  editing = false;
  selectedService: ServiceDto | null = null;

  dataSource: MatTableDataSource<ServiceDto> = new MatTableDataSource<ServiceDto>();
  displayedColumns: string[] = [
    'createdDate',
    'lastModifiedDate',
    'name',
    'description',
    'endpoint',        // Added endpoint here
    'pricing',
    'status',
    'actions',
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  isAdmin = false;
  groups: GroupDto[] = [];
  snackBar: any;

  constructor(
    public dialog: MatDialog,
    private userService: UserService,
    private serviceUsageService: ServiceUsageService,
    private groupService: GroupService,
    private keycloakService: KeycloakService,
    private cdr: ChangeDetectorRef,
    private factureService: FactureService
  ) {}

  ngOnInit(): void {
    this.loadGroups();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.isAdmin = this.keycloakService.hasRole('admin');
      this.cdr.detectChanges();

      if (this.paginator) {
        this.dataSource.paginator = this.paginator;
        this.loadServices();
      }
    });
  }

  openAddApiDialog(): void {
    const dialogRef = this.dialog.open(AddApiComponent, {
      width: '400px',
    });

    dialogRef.afterClosed().subscribe(() => {
      this.loadServices();
    });
  }

  loadGroups(): void {
    this.groupService.getAllGroups().subscribe(
      (groups: GroupDto[]) => {
        this.groups = groups;
        this.cdr.detectChanges();
      },
      (error) => {
        console.error('Error loading groups:', error);
      }
    );
  }

  loadServices(searchData?: any): void {
    if (!this.paginator) {
      console.error('Paginator is not initialized.');
      return;
    }

    merge(this.paginator.page, of({}))
      .pipe(
        switchMap(() => {
          const pageIndex: number = this.paginator.pageIndex;
          const pageSize: number = this.paginator.pageSize;
          if (searchData) {
            return this.serviceUsageService.performAdvancedSearch(searchData);
          } else if (this.isAdmin) {
            return this.serviceUsageService.findAllDtos(true, pageIndex, pageSize);
          } else {
            return this.groupService.getAllGroups().pipe(
              switchMap((groups: any[]) => {
                const userGroup = groups[0];
                return this.groupService.getGroupApis(userGroup.id);
              })
            );
          }
        })
      )
      .subscribe(
        (data: any) => {
          this.dataSource.data = data.content || data.data || [];
          this.cdr.detectChanges();
        },
        (error) => {
          console.error('Error loading services:', error);
          this.dataSource.data = [];
          this.cdr.detectChanges();
        }
      );
  }

  submitSearch(): void {
    const searchData = {
      searchData: {
        search: {
          name: { likeValue: `%${this.searchQuery}%` },
        },
        searchs: [],
        operation: 'OR',
      },
      size: 5,
      page: 0,
    };

    this.loadServices(searchData);
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.searchQuery = filterValue.trim().toLowerCase(); // Update searchQuery
    this.dataSource.filter = this.searchQuery;
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  deleteService(service: ServiceDto): void {
    this.serviceUsageService.deleteService(service.id || '').subscribe(
      () => {
        this.dataSource.data = this.dataSource.data.filter((s) => s !== service);
      },
      (error) => {
        console.error('Error deleting service:', error);
      }
    );
  }

  confirmDelete(service: ServiceDto): void {
    const dialogRef = this.dialog.open(DeleteConfirmationDialogComponent, {
      width: '250px',
      data: { message: 'Are you sure you want to delete this service?' },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === true) {
        this.deleteService(service);
      }
    });
  }

  editService(service: ServiceDto): void {
    const dialogRef = this.dialog.open(EditServiceComponent, {
      width: '500px',
      data: { selectedService: service },
    });

    dialogRef.afterClosed().subscribe(() => {
      this.loadServices();
    });
  }

  openServiceDetails(service: ServiceDto): void {
    const dialogRef = this.dialog.open(ApiDetailsComponentComponent, {
      width: '500px',
      data: { serviceId: service.id },
    });

    dialogRef.afterClosed().subscribe(() => {
      console.log('The dialog was closed');
    });
  }

  toggleServiceStatus(checked: boolean, service: ServiceDto): void {
    service.status = checked;

    if (service.id) {
      const methodName = service.status ? 'activateService' : 'deactivateService';
      this.serviceUsageService[methodName](service.id).subscribe(
        () => {
          this.loadServices();
        },
        (error) => {
          service.status = !service.status;
          this.cdr.detectChanges();
        }
      );
    } else {
      this.cdr.detectChanges();
    }
  }

  onGroupSelect(service: ServiceDto, groupId: string): void {
    this.userService.addServiceToGroup(service.id!, groupId).subscribe(
      () => {
        this.loadServices();
      },
      (error) => {
        console.error('Error adding group to service:', error);
      }
    );
  }

  refresh(): void {
    window.location.reload();
  }

  change(event: any) {
    this.serviceUsageService.findAllDtos(true, event.pageIndex, event.pageSize).subscribe((data: any) => {
      this.dataSource.data = data.content || [];
      this.cdr.detectChanges();
    });
  }

  generateInvoices(): void {
    this.factureService.generateInvoices().subscribe(
      () => {
        console.log('Invoices generated successfully');
        this.snackBar.open('Invoices generated successfully', 'Close', { duration: 3000 });
      },
      (error) => {
        console.error('Error generating invoices:', error);
        this.snackBar.open('Failed to generate invoices', 'Close', { duration: 3000 });
      }
    );
  }

  clearSearch() {
    this.searchQuery = '';
    this.applyFilter({ target: { value: '' } } as unknown as Event);
  }
}
