import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListUserComponent } from './list-user/list-user.component';
import { AddUserComponent } from './add-user/add-user.component';
import { EditUserComponent } from './edit-user/edit-user.component';
import { UserDashboardComponent } from '../user-dashboard/user-dashboard.component';
import { UserProfilePageComponent } from '../user-profile-page/user-profile-page.component';
import { AuthGuard } from '../../auth.guard';

const userRoutes: Routes = [
  { path: '', component: UserDashboardComponent, children: [] },
  {
    path: 'user-dashboard',
    component: UserDashboardComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'user-profile',
    component: UserProfilePageComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'list-users',
    component: ListUserComponent,
    canActivate: [AuthGuard],
  },
  { path: 'add-user', component: AddUserComponent, canActivate: [AuthGuard] },
  {
    path: 'edit-user/:id',
    component: EditUserComponent,
    canActivate: [AuthGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(userRoutes)],
  exports: [RouterModule],
})
export class UserRoutingModule {}
