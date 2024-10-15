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
  // Define color scheme for charts
  colorScheme: Color = {
    name: 'custom',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#5AA454', '#E44D25', '#CFC0BB', '#7aa3e5', '#a8385d', '#aae3f5']
  };

  // Color mapping to ensure consistent colors across charts
  colorMap: { [key: string]: string } = {};

  // Chart configurations array to hold each chart's settings
  chartConfigs: any[] = [];

  // Loading states for charts
  isLoading: { [key: string]: boolean } = {};

  constructor(
    private statisticsService: StatisticsService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Define the chart configurations
    this.chartConfigs = [
      {
        id: 'serviceUsage',
        title: 'Service Usage Overview',
        legendTitle: 'Service Call Breakdown',
        data: [],
        valueSuffix: 'calls',
        dataLoader: this.loadServiceUsageStatistics.bind(this)
      },
      {
        id: 'revenueOverview',
        title: 'Revenue Overview',
        legendTitle: 'Revenue by Service',
        data: [],
        valuePrefix: '$',
        valueFormat: '1.2-2',
        dataLoader: this.loadRevenueStatistics.bind(this)
      },
      {
        id: 'userActivity',
        title: 'User Activity Overview',
        legendTitle: 'Usage by User',
        data: [],
        valueSuffix: 'calls',
        dataLoader: this.loadUserUsageStatistics.bind(this)
      },
      {
        id: 'clientRevenue',
        title: 'Client Revenue Overview',
        legendTitle: 'Revenue by Client',
        data: [],
        valuePrefix: '$',
        valueFormat: '1.2-2',
        dataLoader: this.loadGroupRevenueStatistics.bind(this)
      }
    ];

    // Load data for each chart
    this.chartConfigs.forEach(config => {
      this.isLoading[config.id] = true;
      config.dataLoader(config);
    });
  }

  // Get dynamic color for a given item
  getColor(name: string): string {
    if (this.colorMap[name]) {
      return this.colorMap[name];
    }
    const color =
      this.colorScheme.domain[
        Object.keys(this.colorMap).length % this.colorScheme.domain.length
      ];
    this.colorMap[name] = color;
    return color;
  }

  // Load service usage statistics data
  loadServiceUsageStatistics(config: any): void {
    this.statisticsService.getServiceUsageStatistics().subscribe(
      data => {
        config.data = this.transformData(data);
        this.isLoading[config.id] = false;
      },
      error => {
        this.handleError('service usage statistics', error, config.id);
      }
    );
  }

  // Load revenue statistics data
  loadRevenueStatistics(config: any): void {
    this.statisticsService.getRevenueStatistics().subscribe(
      data => {
        config.data = this.transformData(data);
        this.isLoading[config.id] = false;
      },
      error => {
        this.handleError('revenue statistics', error, config.id);
      }
    );
  }

  // Load user usage statistics data
  loadUserUsageStatistics(config: any): void {
    this.statisticsService.getUserUsageStatistics().subscribe(
      data => {
        config.data = this.transformData(data);
        this.isLoading[config.id] = false;
      },
      error => {
        this.handleError('user usage statistics', error, config.id);
      }
    );
  }

  // Load group revenue statistics data
  loadGroupRevenueStatistics(config: any): void {
    this.statisticsService.getGroupRevenueStatistics().subscribe(
      data => {
        config.data = this.transformData(data, true);
        this.isLoading[config.id] = false;
      },
      error => {
        this.handleError('Client revenue statistics', error, config.id);
      }
    );
  }

  // Transform data to a chart-friendly format
  private transformData(data: any, isGroup: boolean = false): any[] {
    return Object.keys(data).map(key => {
      const name = isGroup ? `Client ${key}` : key;
      return {
        name: name,
        value: data[key],
        color: this.getColor(name)
      };
    });
  }

  // Centralized error handling with additional feedback
  private handleError(context: string, error: any, chartId: string): void {
    console.error(`Error fetching ${context}:`, error);
    this.snackBar.open(`Error fetching ${context}`, 'Close', {
      duration: 3000
    });
    this.isLoading[chartId] = false;
  }
}
