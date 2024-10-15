import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatisticsDashboardComponent } from './statistics-dashboard/statistics-dashboard.component';
import { RouterModule, Routes } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule } from '@angular/material/snack-bar';

const routes: Routes = [
  { path: '', component: StatisticsDashboardComponent },
];

@NgModule({
  declarations: [StatisticsDashboardComponent],
  imports: [
    CommonModule,MatIconModule,MatMenuModule,MatButtonModule,MatCardModule,MatSnackBarModule,
    RouterModule.forChild(routes),
    HttpClientModule,
    NgxChartsModule
  ],
  exports: [RouterModule]
})
export class StatisticsModule {}
