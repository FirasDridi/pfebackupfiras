// add-group.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { GroupService } from '../group-service.service';
import { AddUserComponent } from '../../user/add-user/add-user.component';

@Component({
  selector: 'app-add-group',
  templateUrl: './add-group.component.html',
  styleUrls: ['./add-group.component.css']
})
export class AddGroupComponent implements OnInit {
  groupForm!: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private groupService: GroupService,
    private snackBar: MatSnackBar,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.groupForm = this.formBuilder.group({
      name: ['', Validators.required],
      description: ['']
    });
  }

  onSubmit(): void {
    if (this.groupForm.valid) {
      this.groupService.createGroup(this.groupForm.value).subscribe(
        (response: any) => {
          this.snackBar.open('Group added successfully', 'Close', { duration: 3000 });
          this.openAddUserDialog(response.id, this.groupForm.value.name); // Pass group ID and name
        },
        (error: any) => {
          console.error('Error adding group:', error);
          this.snackBar.open('Failed to add group', 'Close', { duration: 3000 });
        }
      );
    }
  }

  openAddUserDialog(groupId: string, groupName: string): void {
    const dialogRef = this.dialog.open(AddUserComponent, {
      width: '400px',
      data: { groupId, groupName }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.router.navigate(['/group/list-group']);
      }
    });
  }

  onCancel(): void {
    // Add your cancel logic here
  }
}
