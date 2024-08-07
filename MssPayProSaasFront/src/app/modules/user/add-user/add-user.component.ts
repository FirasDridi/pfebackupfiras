import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KeycloakService } from '../../keycloak/keycloak.service';

@Component({
  selector: 'app-add-user',
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css']
})
export class AddUserComponent implements OnInit {
  addUserForm!: FormGroup;
  groupName: string;
  role: string;
displayedUserColumns: any;

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private http: HttpClient,
    private dialogRef: MatDialogRef<AddUserComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private keycloakService: KeycloakService
  ) {
    this.groupName = data.groupName;
    this.role = data.role || 'user'; // Default to 'user' if no role is provided
  }

  ngOnInit(): void {
    this.addUserForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      groupName: [{ value: this.groupName, disabled: true }, Validators.required]  // Pre-fill the group name and disable the input
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

      this.http.post(
        `http://localhost:8884/admin/addUserWithRole?roleName=${this.role}`,
        userGroupDTO,
        { headers }
      ).subscribe(
        (response: any) => {
          this.snackBar.open(`User added with role ${this.role} successfully`, 'Close', { duration: 3000 });
          this.dialogRef.close(true);  // Close the dialog and refresh the list
        },
        error => {
          console.error(`Error adding user with role ${this.role}:`, error);
          this.snackBar.open(`Failed to add user with role ${this.role}`, 'Close', { duration: 3000 });
        }
      );
    } else {
      this.snackBar.open('Please fill out the form', 'Close', { duration: 3000 });
    }
  }
}
