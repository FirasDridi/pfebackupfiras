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
      { path: 'all-services', component: AllServicesComponent },  // This will be protected by a superuser check in the component
      { path: '', redirectTo: 'services', pathMatch: 'full' },  // Redirect to services for normal users
    ],
  },
  { path: 'access-denied', component: AccessDeniedComponent },
  { path: 'user/:userId/invoices', component: UserInvoicesComponent, canActivate: [AuthGuard] },
  { path: 'group/:groupId/invoices', component: GroupInvoicesComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: 'user/services', pathMatch: 'full' },  // Redirect to services as the root route for normal users
  { path: '**', redirectTo: 'user/services' },  // Redirect any unknown routes to services
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
