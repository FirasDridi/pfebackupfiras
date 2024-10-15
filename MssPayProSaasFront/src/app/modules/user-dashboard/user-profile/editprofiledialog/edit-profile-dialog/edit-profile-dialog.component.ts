import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-edit-profile-dialog',
  templateUrl: './edit-profile-dialog.component.html',
  styleUrls: ['./edit-profile-dialog.component.css']
})
export class EditProfileDialogComponent {
  editProfileForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<EditProfileDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    // Initialize the form with data passed to the dialog
    this.editProfileForm = this.fb.group({
      firstname: [data.firstname, Validators.required],
      lastName: [data.lastName, Validators.required],
      email: [data.email, [Validators.required, Validators.email]],
      password: ['', [Validators.minLength(6)]] // Password is optional
    });
  }

  saveProfile(): void {
    if (this.editProfileForm.valid) {
      this.dialogRef.close(this.editProfileForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
