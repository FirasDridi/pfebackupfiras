import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FactureService } from '../facture.service';

@Component({
  selector: 'app-user-invoices',
  templateUrl: './user-invoices.component.html',
  styleUrls: ['./user-invoices.component.css']
})
export class UserInvoicesComponent implements OnInit {
  userId!: number;
  invoices: any[] = [];
  totalAmount!: number;
  displayedColumns: string[] = ['id', 'serviceId', 'serviceName', 'timestamp', 'amount'];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private factureService: FactureService
  ) {}

  ngOnInit(): void {
    this.userId = parseInt(this.data.userId, 10);
    if (isNaN(this.userId)) {
      console.error('Invalid User ID:', this.data.userId);
    } else {
      console.log('User ID:', this.userId);
      this.generateAndLoadInvoices();
    }
  }

  generateAndLoadInvoices(): void {
    this.factureService.generateInvoices().subscribe(() => {
      this.loadInvoices();
      this.loadTotalAmount();
    });
  }

  loadInvoices(): void {
    console.log('Loading invoices for user ID:', this.userId);
    this.factureService.getUserInvoices(this.userId).subscribe((data) => {
      this.invoices = data;
      console.log('Invoices:', this.invoices);
    });
  }

  loadTotalAmount(): void {
    console.log('Loading total amount for user ID:', this.userId);
    this.factureService.getUserTotal(this.userId).subscribe((total) => {
      this.totalAmount = total;
      console.log('Total Amount:', this.totalAmount);
    });
  }
}
