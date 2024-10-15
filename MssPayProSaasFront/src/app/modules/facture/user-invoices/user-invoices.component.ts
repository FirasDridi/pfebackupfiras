import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FactureService } from '../facture.service';

@Component({
  selector: 'app-user-invoices',
  templateUrl: './user-invoices.component.html',
  styleUrls: ['./user-invoices.component.css']
})
export class UserInvoicesComponent implements OnInit {
  userId!: string; // This should now be the Keycloak ID
  invoices: any[] = [];
  totalAmount!: number;
  displayedColumns: string[] = ['serviceName', 'timestamp', 'amount'];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private factureService: FactureService
  ) {}

  ngOnInit(): void {
    this.userId = this.data.userId; // Ensure this is the Keycloak ID
    if (!this.userId) {
      console.error('Invalid User ID:', this.data.userId);
    } else {
      console.log('User ID:', this.userId);
      this.generateInvoices();
      this.loadInvoices();
      this.loadTotalAmount();
    }
  }

  generateInvoices(): void {
    console.log('Generating invoices for user ID:', this.userId);
    this.factureService.generateInvoices().subscribe({
      next: () => {
        console.log('Invoices generated successfully');
      },
      error: (error) => {
        console.error('Error generating invoices:', error);
      }
    });
  }

  loadInvoices(): void {
    console.log('Loading invoices for user ID (Keycloak ID):', this.userId);
    this.factureService.getUserInvoices(this.userId).subscribe((data) => {
      this.invoices = data;
      console.log('Invoices:', this.invoices);
    });
  }

  loadTotalAmount(): void {
    console.log('Loading total amount for user ID (Keycloak ID):', this.userId);
    this.factureService.getUserTotal(this.userId).subscribe((total) => {
      this.totalAmount = total;
      console.log('Total Amount:', this.totalAmount);
    });
  }
}
