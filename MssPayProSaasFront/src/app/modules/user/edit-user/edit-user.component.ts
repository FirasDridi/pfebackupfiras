import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { UserService } from '../user.service';
import { UserDTO } from '../user.dto';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.css']
})
export class EditUserComponent implements OnInit {
  editUserForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    public dialogRef: MatDialogRef<EditUserComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user: UserDTO }
  ) {
    // Initialize the form with the correct fields from UserDTO
    this.editUserForm = this.fb.group({
      id: [data.user.id, Validators.required],
      userName: [data.user.userName || data.user.username, Validators.required],
      email: [data.user.email || data.user.emailId, [Validators.required, Validators.email]],
      firstname: [data.user.firstname || data.user.firstName, Validators.required],
      lastName: [data.user.lastName, Validators.required],
      password: [data.user.password, Validators.required]
    });
  }

  ngOnInit(): void {}

  save(): void {
    if (this.editUserForm.valid) {
      this.userService.updateUser(this.editUserForm.value).subscribe(
        (updatedUser) => {
          console.log('User updated successfully', updatedUser);
          this.dialogRef.close(true);
        },
        (error) => {
          console.error('Error updating user:', error);
        }
      );
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
