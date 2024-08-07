import { Component, Inject } from '@angular/core';
import { ClientDto } from '../ClientDto';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ServiceUsageService } from '../../api-service/ServiceUsageService';
import { EditServiceComponent } from '../../api-service/edit-service/edit-service.component';
import { ClientService } from '../ClientServices';

@Component({
  selector: 'app-edit-client',
  templateUrl: './edit-client.component.html',
  styleUrl: './edit-client.component.css'
})
export class EditClientComponent {

  selectedClient : ClientDto;

  constructor(
    public dialogRef: MatDialogRef<EditClientComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private serviceUsageService: ClientService
  ) {
    this.selectedClient = data.selectedClient;

  }

  saveclient(): void {
    if (this.selectedClient && this.selectedClient.id) { // Check if selectedService and id are defined

      this.serviceUsageService.updateClient(this.selectedClient.id, this.selectedClient).subscribe(
        () => {
          console.log('client updated successfully.');
          this.dialogRef.close();
        },
        (error) => {
          console.error('Error updating client:', error);
        }
      );
    } else {
      console.error('client ID is undefined.');
    }
  }

  cancelEdit(): void {
    this.dialogRef.close();
  }

}
