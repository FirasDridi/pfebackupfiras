// src/app/modules/client/client-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AddClientComponent } from './add-client/add-client.component';
import { ListClientComponent } from './list-client/list-client.component';
import { EditClientComponent } from './edit-client/edit-client.component';

const routes: Routes = [
  { path: 'add-client', component: AddClientComponent },
  { path: 'list-client', component: ListClientComponent },
  { path: 'edit-client/:id', component: EditClientComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ClientRoutingModule {}
