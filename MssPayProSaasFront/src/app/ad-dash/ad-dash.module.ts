import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdmindashComponent } from './admindash/admindash.component';
import { AdDashRoutingModule } from './admindash/ad-dash-routing.module';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { AngularMaterialModule } from '../../material.module';
import { MatListItem, MatNavList } from '@angular/material/list';
import { Router, RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ListApiComponent } from '../modules/api-service/list-api/list-api.component';
import { ApiServiceModule } from '../modules/api-service/api-service.module';
import { MatDialogModule } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ApiServiceRoutingModule } from '../modules/api-service/api-service-routing.module';
import { EditServiceComponent } from '../modules/api-service/edit-service/edit-service.component';
import { NotificationsComponent } from './admindash/notifactionRequest/notifications/notifications.component';
import { AdminRequestsComponent } from './admindash/notifactionRequest/admin-requests/admin-requests.component';
import { MatBadgeModule } from '@angular/material/badge';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSnackBarConfig } from '@angular/material/snack-bar';


@NgModule({
  declarations: [AdmindashComponent, NotificationsComponent, AdminRequestsComponent, ],
  imports: [
    CommonModule,
    AdDashRoutingModule,
    MatMenuModule,
    MatSidenavModule,
    MatIconModule,
    MatIconModule,
    MatNavList,
    MatListItem,
    RouterModule,
    MatToolbarModule,
    ApiServiceModule,
    ApiServiceRoutingModule,
    MatDialogModule,
    MatMenuModule,
    MatSidenavModule,
    MatIconModule,
    MatIconModule,
    MatNavList,
    MatListItem,
    RouterModule,
    MatToolbarModule,
    ApiServiceModule,MatBadgeModule,MatSnackBarModule
  ],
  providers: [
    {
      provide: MatSnackBarConfig,
      useValue: { duration: 2500, horizontalPosition: 'right', verticalPosition: 'top' }
    }
  ]
})
export class AdDashModule {}
