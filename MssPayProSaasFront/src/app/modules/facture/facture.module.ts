import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { MatTableModule } from '@angular/material/table';

import { FactureService } from './facture.service';
import { UserInvoicesComponent } from './user-invoices/user-invoices.component';
import { GroupInvoicesComponent } from './group-invoices/group-invoices.component';
import { MatCardModule } from '@angular/material/card';

const routes: Routes = [
  { path: 'user/:userId/invoices', component: UserInvoicesComponent },
  { path: 'group/:groupId/invoices', component: GroupInvoicesComponent }
];

@NgModule({
  declarations: [
    UserInvoicesComponent,
    GroupInvoicesComponent
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    MatCardModule,
    MatTableModule,
    RouterModule.forChild(routes)
  ],
  providers: [FactureService]
})
export class FactureModule { }
