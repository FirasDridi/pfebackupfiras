import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatTableDataSource } from '@angular/material/table';
import { KeycloakService } from '../../../keycloak/keycloak.service';

@Component({
  selector: 'app-user-invoices-new',
  templateUrl: './user-invoices-new.component.html',
  styleUrls: ['./user-invoices-new.component.css']
})
export class UserInvoicesNewComponent implements OnInit {
  dataSource = new MatTableDataSource<any>(); // Initialize dataSource with a MatTableDataSource
  displayedColumns: string[] = ['serviceName', 'userName', 'groupName', 'timestamp', 'amount'];
  currentGroupId: number | null = null;
  loggedInUserId: string | null = '';
  totalAmount: number = 0;

  constructor(private http: HttpClient, private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    this.loggedInUserId = this.keycloakService.getUserId() ?? null;
    if (this.loggedInUserId) {
      this.fetchCurrentUserGroup();
    }
  }

  fetchCurrentUserGroup(): void {
    // Fetch the current user's group information
    this.http.get<any>(`http://localhost:8884/admin/user/${this.loggedInUserId}/groups`).subscribe(
      (groups) => {
        if (groups && groups.length > 0) {
          this.currentGroupId = groups[0].id;
          this.fetchInvoices();
        }
      },
      (error) => {
        console.error('Error fetching user group:', error);
      }
    );
  }

  fetchInvoices(): void {
    this.http.get<any[]>('http://localhost:8084/billing/all-invoices').subscribe(
      (invoices) => {
        // Filter invoices based on the current user's group
        if (this.currentGroupId !== null) {
          this.dataSource.data = invoices.filter(invoice => invoice.groupId === this.currentGroupId);
          this.calculateTotalAmount();
        }
      },
      (error) => {
        console.error('Error fetching invoices:', error);
      }
    );
  }

  calculateTotalAmount(): void {
    this.totalAmount = this.dataSource.data.reduce((sum, invoice) => sum + invoice.amount, 0);
  }

  onPrintThisMonthInvoices(): void {
    const currentMonth = new Date().getMonth();
    const currentYear = new Date().getFullYear();
    const filteredInvoices = this.dataSource.data.filter((invoice: any) => {
      const invoiceDate = new Date(invoice.timestamp);
      return invoiceDate.getMonth() === currentMonth && invoiceDate.getFullYear() === currentYear;
    });

    const printContent = `
      <html>
      <head>
        <title>Monthly Invoices</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 20px; }
          .invoice-container { width: 100%; max-width: 800px; margin: auto; }
          .invoice-title { text-align: center; font-size: 24px; font-weight: bold; margin-bottom: 20px; }
          .invoice-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
          .invoice-table th, .invoice-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
          .invoice-table th { background-color: #f0f0f0; font-weight: bold; }
          .total-amount { text-align: right; font-size: 18px; font-weight: bold; margin-top: 20px; }
        </style>
      </head>
      <body>
        <div class="invoice-container">
          <div class="invoice-title">Monthly Invoices</div>
          <table class="invoice-table">
            <thead>
              <tr>
                <th>Service Name</th>
                <th>User Name</th>
                <th>Client Name</th>
                <th>Date</th>
                <th>Amount</th>
              </tr>
            </thead>
            <tbody>
              ${filteredInvoices.map(invoice => `
                <tr>
                  <td>${invoice.serviceName}</td>
                  <td>${invoice.userName}</td>
                  <td>${invoice.groupName}</td>
                  <td>${new Date(invoice.timestamp).toLocaleString()}</td>
                  <td>${invoice.amount.toFixed(2)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
          <div class="total-amount">
            Total Amount: ${filteredInvoices.reduce((sum, invoice) => sum + invoice.amount, 0).toFixed(2)}
          </div>
        </div>
      </body>
      </html>
    `;

    const printWindow = window.open('', '', 'width=800,height=600');
    if (printWindow) {
      printWindow.document.write(printContent);
      printWindow.document.close();
      printWindow.print();
    }
  }

  onPrint(): void {
    const printContent = `
      <html>
      <head>
        <title>All Invoices</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 20px; }
          .invoice-container { width: 100%; max-width: 800px; margin: auto; }
          .invoice-title { text-align: center; font-size: 24px; font-weight: bold; margin-bottom: 20px; }
          .invoice-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
          .invoice-table th, .invoice-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
          .invoice-table th { background-color: #f0f0f0; font-weight: bold; }
          .total-amount { text-align: right; font-size: 18px; font-weight: bold; margin-top: 20px; }
        </style>
      </head>
      <body>
        <div class="invoice-container">
          <div class="invoice-title">All Invoices</div>
          <table class="invoice-table">
            <thead>
              <tr>
                <th>Service Name</th>
                <th>User Name</th>
                <th>Client Name</th>
                <th>Date</th>
                <th>Amount</th>
              </tr>
            </thead>
            <tbody>
              ${this.dataSource.data.map(invoice => `
                <tr>
                  <td>${invoice.serviceName}</td>
                  <td>${invoice.userName}</td>
                  <td>${invoice.groupName}</td>
                  <td>${new Date(invoice.timestamp).toLocaleString()}</td>
                  <td>${invoice.amount.toFixed(2)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
          <div class="total-amount">
            Total Amount: ${this.totalAmount.toFixed(2)}
          </div>
        </div>
      </body>
      </html>
    `;

    const printWindow = window.open('', '', 'width=800,height=600');
    if (printWindow) {
      printWindow.document.write(printContent);
      printWindow.document.close();
      printWindow.print();
    }
  }
}
