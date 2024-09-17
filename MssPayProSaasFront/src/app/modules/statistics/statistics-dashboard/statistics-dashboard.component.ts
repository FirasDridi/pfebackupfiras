import { Component, OnInit } from '@angular/core';
import { Color, ScaleType } from '@swimlane/ngx-charts';
import { StatisticsService } from './StatisticsService';

@Component({
  selector: 'app-statistics-dashboard',
  templateUrl: './statistics-dashboard.component.html',
  styleUrls: ['./statistics-dashboard.component.css']
})
export class StatisticsDashboardComponent implements OnInit {

  usageChartData: any[] = [];
  revenueChartData: any[] = [];

  colorScheme: Color = {
    name: 'custom',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#5AA454', '#E44D25', '#CFC0BB', '#7aa3e5', '#a8385d', '#aae3f5']
  };


  constructor(private statisticsService: StatisticsService) { }

  ngOnInit(): void {
    this.loadServiceUsageStatistics();
    this.loadRevenueStatistics();
  }

  loadServiceUsageStatistics(): void {
    this.statisticsService.getServiceUsageStatistics().subscribe(data => {
      this.usageChartData = Object.keys(data).map(key => ({
        name: key,
        value: data[key]
      }));
    }, error => {
      console.error('Error fetching service usage statistics:', error);
    });
  }

  loadRevenueStatistics(): void {
    this.statisticsService.getRevenueStatistics().subscribe(data => {
      this.revenueChartData = Object.keys(data).map(key => ({
        name: key,
        value: data[key]
      }));
    }, error => {
      console.error('Error fetching revenue statistics:', error);
    });
  }
}
