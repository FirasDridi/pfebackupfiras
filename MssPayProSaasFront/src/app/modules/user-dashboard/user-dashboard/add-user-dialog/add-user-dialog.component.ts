import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-add-user-dialog',
  templateUrl: './add-user-dialog.component.html',
  styleUrls: ['./add-user-dialog.component.css'],
})
export class AddUserDialogComponent {
  addUserForm: FormGroup;
  selectedGroup: string | null = null;

  constructor(
    public dialogRef: MatDialogRef<AddUserDialogComponent>,
    private fb: FormBuilder,
    private http: HttpClient,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.addUserForm = this.fb.group({
      groupName: [
        { value: this.data.groupName, disabled: true },
        Validators.required,
      ],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });

    this.selectedGroup = this.data.groupName; // Ensure selectedGroup is set
  }

  onSubmit(): void {
    if (this.addUserForm.valid) {
      const userGroupDTO = {
        groupName: this.selectedGroup,
        firstname: this.addUserForm.get('firstName')?.value,
        lastName: this.addUserForm.get('lastName')?.value,
        emailId: this.addUserForm.get('email')?.value,
        password: this.addUserForm.get('password')?.value,
        userName: this.addUserForm.get('email')?.value,
      };

      const headers = new HttpHeaders({
        Authorization: `Bearer ${this.data.token}`, // Assuming token is passed in data
        'Content-Type': 'application/json',
      });

      this.http
        .post<any>(
          `http://localhost:8884/admin/addUsersToGroup?groupName=${this.selectedGroup}`,
          [userGroupDTO],
          { headers }
        )
        .subscribe(
          (response) => {
            console.log(
              `User added successfully to group ${this.selectedGroup}`,
              response
            );
            this.snackBar.open(
              `User added to group ${this.selectedGroup} successfully`,
              'Close',
              { duration: 3000 }
            );
            this.dialogRef.close(true); // Close the dialog and return success
          },
          (error) => {
            console.error(
              `Error adding user to group ${this.selectedGroup}:`,
              error
            );
            this.snackBar.open(
              `Failed to add user to group ${this.selectedGroup}`,
              'Close',
              { duration: 3000 }
            );
          }
        );
    } else {
      this.snackBar.open('Please fill out the form', 'Close', {
        duration: 3000,
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
