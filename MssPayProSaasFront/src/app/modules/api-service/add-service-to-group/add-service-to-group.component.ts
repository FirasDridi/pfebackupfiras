import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ServiceUsageService } from '../ServiceUsageService';
import { Router } from '@angular/router';
import { UserService } from '../../user/user.service';

@Component({
  selector: 'app-add-service-to-group',
  templateUrl: './add-service-to-group.component.html',
  styleUrls: ['./add-service-to-group.component.css']
})
export class AddServiceToGroupComponent implements OnInit {
  serviceForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private userService : UserService,
    private snackBar: MatSnackBar,
    private serviceUsageService: ServiceUsageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.createForm();
  }

  createForm(): void {
    this.serviceForm = this.fb.group({
      serviceId: ['', Validators.required],
      groupId: ['', Validators.required],
    });
  }

  addServiceToGroup(): void {
    if (this.serviceForm.valid) {
      const formData = this.serviceForm.value;
      this.userService.addServiceToGroup(formData.serviceId, formData.groupId).subscribe(
        () => {
          this.snackBar.open('Service added to group successfully', 'Close', {
            duration: 3000,
          });
          this.router.navigate(['/service-group-list']); // Adjust the route as needed
        },
        (error) => {
          console.error('Error adding service to group:', error);
          this.snackBar.open('Error adding service to group', 'Close', {
            duration: 3000,
          });
        }
      );
    }
  }
}
