import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ServiceDto } from '../ServiceDto';
import { ServiceUsageService } from '../ServiceUsageService';
import { Router } from '@angular/router';
import { Observable, map } from 'rxjs';

@Component({
  selector: 'app-add-api',
  templateUrl: './add-api.component.html',
  styleUrls: ['./add-api.component.css'],
})
export class AddApiComponent implements OnInit {
  serviceForm!: FormGroup;
  nameExists: boolean = false;

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private serviceUsageService: ServiceUsageService
  ) {}

  ngOnInit(): void {
    this.createForm();
  }

  createForm(): void {
    this.serviceForm = this.fb.group({
      name: ['', [Validators.required], this.validateName.bind(this)],
      pricing: ['', [Validators.required, Validators.pattern(/^\d*\.?\d+$/)]],
      description: [''],
      endpoint: ['', [Validators.required]],  // Add the endpoint field
      status: [true],
    });
  }


  validateName(
    control: AbstractControl
  ): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> {
    return this.serviceUsageService.existsByName(control.value).pipe(
      map((exists: boolean) => {
        this.nameExists = exists; // Set the flag based on the result
        return exists ? { nameExists: true } : null; // Return validation error if name exists
      })
    );
  }

  checkNameExists(): void {
    const name = this.serviceForm.get('name')?.value;
    this.serviceUsageService.existsByName(name).subscribe(
      (exists) => {
        this.nameExists = exists;
        if (exists) {
          this.showSnackBar(
            'Service name already exists. Please choose a different name.'
          );
        }
      },
      (error) => {
        console.error('Error checking service name existence:', error);
      }
    );
  }

  createService(): void {
    // Check if the name already exists before saving
    this.checkNameExists();

    // Proceed with saving only if the name does not exist
    if (!this.nameExists) {
      const serviceData: ServiceDto = this.serviceForm.value;
      this.serviceUsageService.save(serviceData).subscribe(
        () => {
          console.log('Service created successfully.');
          this.clearForm();
          this.showSnackBar('Service created successfully.');
          // Reload the current route
          this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
            this.router.navigate(['/admins/list']);
          });
        },
        (error) => {
          console.error('Error creating service:', error);
        }
      );
    }
  }


  clearForm(): void {
    this.serviceForm.reset();
  }

  // Method to show a snack bar message
  showSnackBar(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000, // Duration in milliseconds
    });
  }
}
