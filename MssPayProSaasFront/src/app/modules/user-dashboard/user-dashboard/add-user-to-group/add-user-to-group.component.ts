import { Component, OnInit, Input } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { KeycloakService } from '../../../keycloak/keycloak.service';

@Component({
  selector: 'app-add-user-to-group',
  templateUrl: './add-user-to-group.component.html',
  styleUrls: ['./add-user-to-group.component.css']
})
export class AddUserToGroupComponent implements OnInit {
  @Input() groupName!: string;
  addUserForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private keycloakService: KeycloakService
  ) {}

  ngOnInit(): void {
    this.addUserForm = this.fb.group({
      groupName: [{ value: this.groupName, disabled: true }, Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.addUserForm.valid) {
      const userGroupDTO = {
        groupName: this.groupName,
        firstname: this.addUserForm.get('firstName')?.value,
        lastName: this.addUserForm.get('lastName')?.value,
        emailId: this.addUserForm.get('email')?.value,
        password: this.addUserForm.get('password')?.value,
        userName: this.addUserForm.get('email')?.value // Assuming username is the email
      };

      const headers = new HttpHeaders({
        'Authorization': `Bearer ${this.keycloakService.getToken()}`,
        'Content-Type': 'application/json'
      });

      this.http.post<any>(`http://localhost:8884/admin/addUsersToGroup?groupName=${this.groupName}`, [userGroupDTO], { headers }).subscribe(
        (response) => {
          console.log(`User added successfully to group ${this.groupName}`, response);
          this.snackBar.open(`User added to group ${this.groupName} successfully`, 'Close', { duration: 3000 });
          // Add any additional actions you want to perform on successful form submission
        },
        (error) => {
          console.error(`Error adding user to group ${this.groupName}:`, error);
          this.snackBar.open(`Failed to add user to group ${this.groupName}`, 'Close', { duration: 3000 });
        }
      );
    } else {
      this.snackBar.open('Please fill out the form', 'Close', { duration: 3000 });
    }
  }
}
