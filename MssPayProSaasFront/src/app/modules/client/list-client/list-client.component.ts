import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  OnInit,
  ViewChild,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { merge, of } from 'rxjs';
import { switchMap, map } from 'rxjs/operators';
import { ClientDto } from '../ClientDto';
import { ClientService } from '../ClientServices';
import { EditClientComponent } from '../edit-client/edit-client.component';

@Component({
  selector: 'app-list-client',
  templateUrl: './list-client.component.html',
  styleUrls: ['./list-client.component.css'],
})
export class ListClientComponent implements OnInit, AfterViewInit {
  searchQuery: string = '';
  editing = false;
  selectedClient: ClientDto | null = null;
  dataSource: MatTableDataSource<ClientDto> = new MatTableDataSource<ClientDto>();
  displayedColumns: string[] = [
    'name',
    'prenom',
    'email',
    'motDePasse',
    'adresse',
    'numeroDeTelephone',
    'isActive',
    'billingAddress',
    'packageId',
    'packageName',
    'edit',
    'actions',
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    public dialog: MatDialog,
    private clientService: ClientService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadClients();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.paginator._intl.itemsPerPageLabel = 'Items per page';
    this.loadClients();
  }

  refresh(): void {
    window.location.reload();
  }

  loadClients(searchData?: any): void {
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
            return this.clientService.performAdvancedSearch(searchData);
          } else {
            return this.clientService.findAllDtos(true, pageIndex, pageSize);
          }
        })
      )
      .subscribe(
        (data: any) => {
          this.dataSource.data = data.content || data.data || [];
        },
        (error) => {
          console.error('Error loading clients:', error);
          this.dataSource.data = [];
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

    this.loadClients(searchData);
  }

  deleteClient(client: ClientDto): void {
    this.clientService.deleteClient(client.id || '').subscribe(
      () => {
        this.dataSource.data = this.dataSource.data.filter((c) => c !== client);
        console.log('Client deleted successfully.');
      },
      (error) => {
        console.error('Error deleting client:', error);
      }
    );
  }

  editClient(client: ClientDto): void {
    const dialogRef = this.dialog.open(EditClientComponent, {
      width: '500px',
      data: { selectedClient: client },
    });

    dialogRef.afterClosed().subscribe(() => {
      this.loadClients();
    });
  }

  saveClient(): void {
    if (this.selectedClient && this.selectedClient.id) {
      this.clientService.updateClient(this.selectedClient.id, this.selectedClient).subscribe(
        () => {
          console.log('Client updated successfully.');
          this.loadClients();
          this.cancelEdit();
        },
        (error) => {
          console.error('Error updating client:', error);
        }
      );
    }
  }

  toggleClientStatus(checked: boolean, client: ClientDto): void {
    console.log('Toggling client status:', client);
    client.isActive = checked;

    const methodName = client.isActive ? 'activateClient' : 'deactivateClient';

    if (client.id) {
      this.clientService[methodName](client.id).subscribe(
        () => {
          console.log(`Client ${methodName === 'activateClient' ? 'activated' : 'deactivated'} successfully`);
          this.loadClients();
        },
        (error) => {
          console.error(`Error ${methodName === 'activateClient' ? 'activating' : 'deactivating'} client:`, error);
          if (methodName === 'deactivateClient') {
            client.isActive = !client.isActive;
          }
        }
      );
    } else {
      console.error('Client ID is undefined.');
    }
  }

  cancelEdit(): void {
    this.editing = false;
    this.selectedClient = null;
  }
}
