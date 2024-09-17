import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FactureService } from '../facture.service';

@Component({
  selector: 'app-group-invoices',
  templateUrl: './group-invoices.component.html',
  styleUrls: ['./group-invoices.component.css']
})
export class GroupInvoicesComponent implements OnInit {
  groupId!: number;
  invoices: any[] = [];
  totalAmount!: number;
  displayedColumns: string[] = ['serviceName', 'timestamp', 'amount'];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private factureService: FactureService
  ) {}

  ngOnInit(): void {
    this.groupId = Number(this.data.groupId);
    this.loadInvoices();  // Load invoices after the dialog is opened
    this.loadTotalAmount();  // Load total amount after the dialog is opened
  }

  loadInvoices(): void {
    this.factureService.getGroupInvoices(this.groupId).subscribe((data) => {
      this.invoices = data;
      console.log('Invoices:', this.invoices);
    }, (error) => {
      console.error('Error fetching group invoices:', error);
    });
  }

  loadTotalAmount(): void {
    this.factureService.getGroupTotal(this.groupId).subscribe((total) => {
      this.totalAmount = total;
      console.log('Total Amount:', this.totalAmount);
    }, (error) => {
      console.error('Error fetching total amount:', error);
    });
  }
}
