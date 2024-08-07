import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListApiComponent } from './list-api/list-api.component';
import { AddApiComponent } from './add-api/add-api.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'list',
    pathMatch: 'full',
  },
  {
    path: 'list',
    component: ListApiComponent,
  },
  {
    path: 'add',
    component: AddApiComponent,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ApiServiceRoutingModule { }
