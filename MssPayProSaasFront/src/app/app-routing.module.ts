import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { AccessDeniedComponent } from './modules/access-denied-component/access-denied-component.component';
import { MyAccountComponent } from './modules/my-account/my-account.component';
import { UserInvoicesComponent } from './modules/facture/user-invoices/user-invoices.component';
import { GroupInvoicesComponent } from './modules/facture/group-invoices/group-invoices.component';
import { LayoutComponent } from './modules/user-dashboard/user-dashboard/layout/layout.component';
import { UserDashboardComponent } from './modules/user-dashboard/user-dashboard.component';
import { UserProfileComponent } from './modules/user-dashboard/user-profile/user-profile.component';
import { UserServicesComponent } from './modules/user-dashboard/user-services/user-services.component';
import { AllServicesComponent } from './modules/user-dashboard/all-services/all-services.component';
import { UserStatisticsComponent } from './modules/user-dashboard/user-dashboard/user-statistics/user-statistics/user-statistics.component';
import { UserInvoicesNewComponent } from './modules/user-dashboard/user-dashboard/user-invoices-new/user-invoices-new.component';

const routes: Routes = [
  { path: 'admins', loadChildren: () => import('./ad-dash/ad-dash.module').then(m => m.AdDashModule) },
  {
    path: 'user',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'user-dashboard', component: UserDashboardComponent },
      { path: 'profile', component: UserProfileComponent },
      { path: 'services', component: UserServicesComponent },
      { path: 'all-services', component: AllServicesComponent },
      { path: 'statistics', component: UserStatisticsComponent },
      { path: 'user-invoices-new', component: UserInvoicesNewComponent }, // Add the new route for invoices

      // Add statistics route
      { path: '', redirectTo: 'profile', pathMatch: 'full' },  // Redirect to profile for both normal and super users
    ],
  },
  { path: 'access-denied', component: AccessDeniedComponent },
  { path: 'user/:userId/invoices', component: UserInvoicesComponent, canActivate: [AuthGuard] },
  { path: 'group/:groupId/invoices', component: GroupInvoicesComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: 'user/profile', pathMatch: 'full' },  // Redirect to profile as the root route for all users
  { path: '**', redirectTo: 'user/profile' },  // Redirect any unknown routes to profile
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
