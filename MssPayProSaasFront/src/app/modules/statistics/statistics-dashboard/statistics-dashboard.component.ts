import { Component, OnInit } from '@angular/core';
import { Color, ScaleType } from '@swimlane/ngx-charts';
import { MatSnackBar } from '@angular/material/snack-bar';
import { StatisticsService } from './StatisticsService';

@Component({
  selector: 'app-statistics-dashboard',
  templateUrl: './statistics-dashboard.component.html',
  styleUrls: ['./statistics-dashboard.component.css']
})
export class StatisticsDashboardComponent implements OnInit {

  usageChartData: any[] = [];
  revenueChartData: any[] = [];
  userUsageChartData: any[] = [];
  groupUsageChartData: any[] = [];
  userRevenueChartData: any[] = [];
  groupRevenueChartData: any[] = [];

  colorScheme: Color = {
    name: 'custom',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#5AA454', '#E44D25', '#CFC0BB', '#7aa3e5', '#a8385d', '#aae3f5']
  };

  // Define a color mapping
  colorMap: { [key: string]: string } = {};

  constructor(
    private statisticsService: StatisticsService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.loadServiceUsageStatistics();
    this.loadRevenueStatistics();
    this.loadUserUsageStatistics();
    this.loadGroupUsageStatistics();
    this.loadUserRevenueStatistics();
    this.loadGroupRevenueStatistics();
  }

  // Dynamic color assignment based on name
  getColor(name: string): string {
    if (this.colorMap[name]) {
      return this.colorMap[name];
    }
    const color = this.colorScheme.domain[Object.keys(this.colorMap).length % this.colorScheme.domain.length];
    this.colorMap[name] = color;
    return color;
  }

  loadServiceUsageStatistics(): void {
    this.statisticsService.getServiceUsageStatistics().subscribe(data => {
      this.usageChartData = Object.keys(data).map(key => ({
        name: key,
        value: data[key],
        color: this.getColor(key)
      }));
    }, error => {
      console.error('Error fetching service usage statistics:', error);
      this.snackBar.open('Error fetching service usage statistics', 'Close', {
        duration: 3000,
      });
    });
  }

  loadRevenueStatistics(): void {
    this.statisticsService.getRevenueStatistics().subscribe(data => {
      this.revenueChartData = Object.keys(data).map(key => ({
        name: key,
        value: data[key],
        color: this.getColor(key)
      }));
    }, error => {
      console.error('Error fetching revenue statistics:', error);
      this.snackBar.open('Error fetching revenue statistics', 'Close', {
        duration: 3000,
      });
    });
  }

  // New methods to load aggregated statistics

  loadUserUsageStatistics(): void {
    this.statisticsService.getUserUsageStatistics().subscribe(data => {
      this.userUsageChartData = Object.keys(data).map(key => ({
        name: key,
        value: data[key],
        color: this.getColor(key)
      }));
    }, error => {
      console.error('Error fetching user usage statistics:', error);
      this.snackBar.open('Error fetching user usage statistics', 'Close', {
        duration: 3000,
      });
    });
  }

  loadGroupUsageStatistics(): void {
    this.statisticsService.getGroupUsageStatistics().subscribe(data => {
      this.groupUsageChartData = Object.keys(data).map(key => ({
        name: `Group ${key}`,
        value: data[key as any],
        color: this.getColor(`Group ${key}`)
      }));
    }, error => {
      console.error('Error fetching group usage statistics:', error);
      this.snackBar.open('Error fetching group usage statistics', 'Close', {
        duration: 3000,
      });
    });
  }

  loadUserRevenueStatistics(): void {
    this.statisticsService.getUserRevenueStatistics().subscribe(data => {
      this.userRevenueChartData = Object.keys(data).map(key => ({
        name: key,
        value: data[key],
        color: this.getColor(key)
      }));
    }, error => {
      console.error('Error fetching user revenue statistics:', error);
      this.snackBar.open('Error fetching user revenue statistics', 'Close', {
        duration: 3000,
      });
    });
  }

  loadGroupRevenueStatistics(): void {
    this.statisticsService.getGroupRevenueStatistics().subscribe(data => {
      this.groupRevenueChartData = Object.keys(data).map(key => ({
        name: `Group ${key}`,
        value: data[key as any],
        color: this.getColor(`Group ${key}`)
      }));
    }, error => {
      console.error('Error fetching group revenue statistics:', error);
      this.snackBar.open('Error fetching group revenue statistics', 'Close', {
        duration: 3000,
      });
    });
  }
}
