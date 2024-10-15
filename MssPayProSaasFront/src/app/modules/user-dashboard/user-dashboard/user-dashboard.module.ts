import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatDialogModule } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { PanelModule } from 'primeng/panel';
import { ButtonModule } from 'primeng/button';
import { MenubarModule } from 'primeng/menubar';

import { UserDashboardComponent } from '../user-dashboard.component';
import { UserProfileComponent } from '../user-profile/user-profile.component';
import { UserServicesComponent } from '../user-services/user-services.component';
import { LayoutComponent } from './layout/layout.component';
import { AllServicesComponent } from '../all-services/all-services.component';
import { LoadingDialogComponent } from '../loading-dialog/loading-dialog.component';
import { ServiceDetailsDialogComponent } from '../service-details-popup/service-details-popup.component';
import { AddUserToGroupComponent } from './add-user-to-group/add-user-to-group.component';
import { UserModule } from '../../user/user.module';
import { MatTabsModule } from '@angular/material/tabs';
import { UserRoutingModule } from '../../user/user-routing.module';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { AddUserDialogComponent } from './add-user-dialog/add-user-dialog.component';
import { MatListModule } from '@angular/material/list';
import { UserStatisticsComponent } from './user-statistics/user-statistics/user-statistics.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UserInvoicesNewComponent } from './user-invoices-new/user-invoices-new.component';

@NgModule({
  declarations: [
    UserDashboardComponent,
    UserProfileComponent,
    UserServicesComponent,
    AllServicesComponent,
    LayoutComponent,
    LoadingDialogComponent,
    ServiceDetailsDialogComponent,
    AddUserToGroupComponent,
    AddUserDialogComponent,
    UserStatisticsComponent,
    UserInvoicesNewComponent
  ],
  imports: [
    CommonModule,
    MatMenuModule,
    RouterModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatToolbarModule,
    MatDialogModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    PanelModule,
    ButtonModule,
    MenubarModule,
    MatTabsModule,
    UserModule,
    MatCardModule,
    NgxChartsModule,
    MatTooltipModule,MatCardModule // Import UserModule to use AddUserComponent
    // UserRoutingModule,
  ],
  exports: [
    UserDashboardComponent,
    UserProfileComponent,
    UserServicesComponent,
    AllServicesComponent,
  ],
})
export class UserDashboardModule {
  constructor() {
    console.log('UserDashboardModule loaded');
  }
}
