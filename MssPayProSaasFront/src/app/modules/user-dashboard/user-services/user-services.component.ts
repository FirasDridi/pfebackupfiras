import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-user-services',
  templateUrl: './user-services.component.html',
  styleUrls: ['./user-services.component.css']
})
export class UserServicesComponent implements OnInit {
  services: any[] = [];
  dataSource: MatTableDataSource<any> = new MatTableDataSource<any>();
  displayedColumns: string[] = ['service', 'timestamp'];

  constructor(private http: HttpClient, private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    const userId = this.keycloakService.getUserId();
    if (userId) {
      this.http.get<any[]>(`http://localhost:8033/api/v1/consumption/user/${userId}/services`).subscribe(
        (data) => {
          this.services = data;
          this.dataSource.data = this.services;
        },
        (error) => {
          console.error('Error fetching service history:', error);
        }
      );
    }
  }
}
