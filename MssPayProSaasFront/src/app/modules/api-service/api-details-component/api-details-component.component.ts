import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ServiceUsageService } from '../ServiceUsageService';
import { ServiceDetailsDto } from '../ServiceDetailsDto';

@Component({
  selector: 'app-api-details-component',
  templateUrl: './api-details-component.component.html',
  styleUrls: ['./api-details-component.component.css']
})
export class ApiDetailsComponentComponent implements OnInit {
  serviceDetails: ServiceDetailsDto | null = null;
  serviceId: string;

  constructor(
    private dialogRef: MatDialogRef<ApiDetailsComponentComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { serviceId: string },
    private serviceUsageService: ServiceUsageService
  ) {
    this.serviceId = data.serviceId;
  }

  ngOnInit(): void {
    this.loadServiceDetails();
  }

  loadServiceDetails(): void {
    this.serviceUsageService.getServicesWithDetails().subscribe(
      (details: ServiceDetailsDto[]) => {
        this.serviceDetails = details.find(detail => detail.id === this.serviceId) || null;
      },
      (error) => {
        console.error('Error loading service details:', error);
      }
    );
  }

  close(): void {
    this.dialogRef.close();
  }
}
