// src/app/modules/facture/facture.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { FactureService } from './facture.service';
import { UserInvoicesComponent } from './user-invoices/user-invoices.component';
import { GroupInvoicesComponent } from './group-invoices/group-invoices.component';
import { MatTableModule } from '@angular/material/table';

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
    MatTableModule,
    RouterModule.forChild(routes)
  ],
  providers: [FactureService]
})
export class FactureModule { }
