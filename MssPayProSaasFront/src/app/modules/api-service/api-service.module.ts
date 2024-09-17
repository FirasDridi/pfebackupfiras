import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AddApiComponent } from './add-api/add-api.component';
import { ListApiComponent } from './list-api/list-api.component';
import { EditServiceComponent } from './edit-service/edit-service.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiDetailsComponentComponent } from './api-details-component/api-details-component.component';
import { AddServiceToGroupComponent } from './add-service-to-group/add-service-to-group.component';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { HttpClientModule } from '@angular/common/http';
import { AdmindashComponent } from '../../ad-dash/admindash/admindash.component';
import { DeleteConfirmationDialog } from './add-api/delete-confirmation-dialog/delete-confirmation-dialog.component';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@NgModule({
  declarations: [
    AddApiComponent,
    ListApiComponent,
    EditServiceComponent,
    ApiDetailsComponentComponent,
    AddServiceToGroupComponent,
    DeleteConfirmationDialog,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTableModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatButtonModule,
    MatDialogModule,
    MatPaginatorModule,
    MatCardModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatOptionModule,
    HttpClientModule,MatSlideToggleModule
  ],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTableModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatButtonModule,
    MatDialogModule,
    MatPaginatorModule,
    MatCardModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatOptionModule,
    HttpClientModule,
  ],
})
export class ApiServiceModule {}
