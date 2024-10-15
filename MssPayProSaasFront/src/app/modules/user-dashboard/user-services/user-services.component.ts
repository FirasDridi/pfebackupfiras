import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { MatTableDataSource } from '@angular/material/table';
import { ServiceDto } from '../../api-service/ServiceDto';

@Component({
  selector: 'app-user-services',
  templateUrl: './user-services.component.html',
  styleUrls: ['./user-services.component.css'],
})
export class UserServicesComponent implements OnInit {
  services: any[] = [];
  dataSource: MatTableDataSource<any> = new MatTableDataSource<any>();
  displayedColumns: string[] = ['service', 'timestamp', 'cost'];
  totalCost: number = 0;

  constructor(private http: HttpClient, private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    const userId = this.keycloakService.getUserId(); // Fetch Keycloak ID of the connected user
    if (userId) {
      // Fetching the list of services consumed by the user based on Keycloak ID
      this.http
        .get<any[]>(`http://localhost:8033/api/v1/consumption/user/${userId}/services`)
        .subscribe(
          (data) => {
            this.services = data;
            this.fetchServicePrices(); // Fetch prices for each service using endpoint
          },
          (error) => {
            console.error('Error fetching service history:', error);
          }
        );
    }
  }

  fetchServicePrices(): void {
    // Fetching all available services
    this.http.get<any>(`http://localhost:8081/service/api/services/list`).subscribe(
      (response) => {
        const allServices = response.content; // Extracting the list of all services
        let total = 0;

        // Iterate over the services consumed by the user and find their price
        this.services.forEach((userService) => {
          const matchedService = allServices.find(
            (service: ServiceDto) => service.endpoint === userService.endpoint
          );

          if (matchedService) {
            userService.pricing = parseFloat(matchedService.pricing);
            total += userService.pricing;
          } else {
            userService.pricing = 'N/A';
          }
        });

        this.totalCost = total;
        this.dataSource.data = [...this.services]; // Update dataSource to reflect changes
      },
      (error) => {
        console.error('Error fetching all services:', error);
      }
    );
  }
}
