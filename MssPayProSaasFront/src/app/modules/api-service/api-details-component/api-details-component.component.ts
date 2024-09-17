import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ServiceUsageService } from '../ServiceUsageService';

@Component({
  selector: 'app-api-details-component',
  templateUrl: './api-details-component.component.html',
  styleUrls: ['./api-details-component.component.css']
})
export class ApiDetailsComponentComponent implements OnInit {
  groupedServiceDetails: any[] = []; // Holds the grouped invoice details
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
    this.serviceUsageService.getInvoicesByServiceId(this.serviceId).subscribe(
      (invoices: any[]) => {
        this.groupedServiceDetails = this.groupByUserAndDate(invoices);
      },
      (error) => {
        console.error('Error loading service details:', error);
      }
    );
  }

  groupByUserAndDate(invoices: any[]): any[] {
    const grouped = invoices.reduce((acc, invoice) => {
      const date = new Date(invoice.timestamp).toLocaleDateString();
      const key = `${invoice.userName}-${date}`;

      if (!acc[key]) {
        acc[key] = {
          userName: invoice.userName,
          date: date,
          serviceName: invoice.serviceName,
          details: []
        };
      }
      acc[key].details.push(invoice);
      return acc;
    }, {});

    return Object.values(grouped);
  }

  close(): void {
    this.dialogRef.close();
  }
}
