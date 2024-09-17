import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatisticsDashboardComponent } from './statistics-dashboard/statistics-dashboard.component';
import { RouterModule, Routes } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { NgxChartsModule } from '@swimlane/ngx-charts';

const routes: Routes = [
  { path: '', component: StatisticsDashboardComponent },
];

@NgModule({
  declarations: [StatisticsDashboardComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    HttpClientModule,
    NgxChartsModule
  ],
  exports: [RouterModule]
})
export class StatisticsModule {}
