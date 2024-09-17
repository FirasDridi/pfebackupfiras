import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdmindashComponent } from './admindash.component';
import { RouterModule, Routes } from '@angular/router';
import { ListUserComponent } from '../../modules/user/list-user/list-user.component';
import { ApiDetailsComponentComponent } from '../../modules/api-service/api-details-component/api-details-component.component';
import { ListGroupComponent } from '../../modules/group/list-group/list-group.component';
import { AddUserComponent } from '../../modules/user/add-user/add-user.component';
import { EditUserComponent } from '../../modules/user/edit-user/edit-user.component';
import { GroupUsersComponent } from '../../modules/group/group-users/group-users.component';
import { AddApiComponent } from '../../modules/api-service/add-api/add-api.component';
import { AddGroupComponent } from '../../modules/group/add-group/add-group.component';
import { AddServiceToGroupComponent } from '../../modules/api-service/add-service-to-group/add-service-to-group.component';
import { EditServiceComponent } from '../../modules/api-service/edit-service/edit-service.component';
import { ListApiComponent } from '../../modules/api-service/list-api/list-api.component';
import { MyAccountComponent } from '../../modules/my-account/my-account.component';
import { NotificationsComponent } from './notifactionRequest/notifications/notifications.component';
import { AdminRequestsComponent } from './notifactionRequest/admin-requests/admin-requests.component';

const routes: Routes = [
  {
    path: '',
    component: AdmindashComponent,
    children: [
      { path: '', redirectTo: 'statistics', pathMatch: 'full' }, // Redirect to statistics by default
      { path: 'statistics', loadChildren: () => import('../../modules/statistics/statistics.module').then(m => m.StatisticsModule) }, // Load the Statistics module
      { path: 'notifications', component: NotificationsComponent },
      { path: 'userlist', component: ListUserComponent },
      { path: 'api/list', component: ApiDetailsComponentComponent },
      { path: 'groups', component: ListGroupComponent },
      { path: 'user/add-user', component: AddUserComponent },
      { path: 'user/edit-user/:id', component: EditUserComponent },
      { path: 'group/:groupId/users', component: GroupUsersComponent },
      { path: 'api/add', component: AddApiComponent },
      { path: 'group/add-group', component: AddGroupComponent },
      { path: 'add-service-to-group', component: AddServiceToGroupComponent },
      { path: 'editservices', component: EditServiceComponent },
      { path: 'ApiDetails', component: ApiDetailsComponentComponent },
      { path: 'list', component: ListApiComponent },
      { path: 'add', component: AddApiComponent },
      { path: 'my-account', component: MyAccountComponent },
    ],
  },
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes)],
})
export class AdDashRoutingModule {}
