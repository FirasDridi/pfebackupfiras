import { Component } from '@angular/core';
import { ClientDto } from '../ClientDto';
import { ClientService } from '../ClientServices';

@Component({
  selector: 'app-add-client',
  templateUrl: './add-client.component.html',
  styleUrl: './add-client.component.css'
})
export class AddClientComponent {
  newClient: ClientDto = new ClientDto();

  constructor(private clientService: ClientService) {}

  onSubmit() {
    // Call the createClient method from the clientService to add a new client
    this.clientService.createClient(this.newClient).subscribe(
      (response) => {
        console.log('Client added successfully:', response);
        // Optionally, you can reset the form after successful submission
        this.newClient = new ClientDto();
      },
      (error) => {
        console.error('Error adding client:', error);
        // Handle error appropriately (e.g., display error message)
      }
    );
  }

}
