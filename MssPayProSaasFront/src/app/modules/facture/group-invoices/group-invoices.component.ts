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
  displayedColumns: string[] = ['userId', 'serviceName', 'timestamp', 'amount'];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private factureService: FactureService
  ) {}

  ngOnInit(): void {
    this.groupId = parseInt(this.data.groupId, 10);
    if (isNaN(this.groupId)) {
      console.error('Invalid Group ID:', this.data.groupId);
    } else {
      console.log('Group ID:', this.groupId);
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
    console.log('Loading invoices for group ID:', this.groupId);
    this.factureService.getGroupInvoices(this.groupId).subscribe((data) => {
      this.invoices = data;
      console.log('Invoices:', this.invoices);
    });
  }

  loadTotalAmount(): void {
    console.log('Loading total amount for group ID:', this.groupId);
    this.factureService.getGroupTotal(this.groupId).subscribe((total) => {
      this.totalAmount = total;
      console.log('Total Amount:', this.totalAmount);
    });
  }
}
