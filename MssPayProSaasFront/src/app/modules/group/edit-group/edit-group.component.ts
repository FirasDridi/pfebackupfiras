import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { GroupService } from '../group-service.service';
import { UserGroupDTO } from '../group/UserGroupDTO';

@Component({
  selector: 'app-edit-group',
  templateUrl: './edit-group.component.html',
  styleUrls: ['./edit-group.component.css']
})
export class EditGroupComponent implements OnInit {
  groupForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private groupService: GroupService,
    public dialogRef: MatDialogRef<EditGroupComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { group: UserGroupDTO }
  ) {}

  ngOnInit(): void {
    this.groupForm = this.fb.group({
      id: [this.data.group.id],
      name: [this.data.group.firstName, Validators.required]
      // Removed description field
    });
  }

  onSubmit(): void {
    if (this.groupForm.valid) {
      this.groupService.updateGroup(this.groupForm.value.id, this.groupForm.value).subscribe(
        response => {
          console.log('Group updated successfully:', response);
          this.dialogRef.close();
        },
        error => {
          console.error('Error updating group:', error);
        }
      );
    }
  }
}
