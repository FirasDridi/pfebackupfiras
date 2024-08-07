// edit-service.component.ts

import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ServiceDto } from '../ServiceDto';
import { ServiceUsageService } from '../ServiceUsageService';

@Component({
  selector: 'app-edit-service',
  templateUrl: './edit-service.component.html',
  styleUrls: ['./edit-service.component.css']
})
export class EditServiceComponent {

  selectedService: ServiceDto;

  constructor(
    public dialogRef: MatDialogRef<EditServiceComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private serviceUsageService: ServiceUsageService
  ) {
    this.selectedService = data.selectedService;

  }
  isPricingValid(): boolean {
    const pricing = this.selectedService.pricing;
    return pricing != null && /^\d*\.?\d+$/.test(pricing.toString());
  }
  saveService(): void {
    if (this.selectedService && this.selectedService.id) {
      this.serviceUsageService.updateService(this.selectedService.id, this.selectedService).subscribe(
        () => {
          console.log('Service updated successfully.');
          this.dialogRef.close();
        },
        (error) => {
          console.error('Error updating service:', error);
        }
      );
    } else {
      console.error('Service ID is undefined.');
    }
  }

  cancelEdit(): void {
    this.dialogRef.close();
  }

}
