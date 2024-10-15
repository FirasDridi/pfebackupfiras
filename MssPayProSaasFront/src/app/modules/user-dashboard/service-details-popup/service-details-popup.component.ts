import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-service-details-dialog',
  template: `
    <h1 mat-dialog-title>Service Details</h1>
    <div mat-dialog-content>
      <pre>{{ data.message }}</pre>
    </div>
    <div mat-dialog-actions>
      <button mat-button (click)="closeDialog()">Close</button>
    </div>
  `,
})
export class ServiceDetailsDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<ServiceDetailsDialogComponent>,
    private http: HttpClient
  ) {}

  closeDialog(): void {
    this.http.delete('http://localhost:8081/service/api/logs/clear').subscribe(
      () => {
        console.log('Logs cleared');
        this.dialogRef.close();
      },
      (error) => {
        console.error('Error clearing logs:', error);
        this.dialogRef.close();
      }
    );
  }
}
