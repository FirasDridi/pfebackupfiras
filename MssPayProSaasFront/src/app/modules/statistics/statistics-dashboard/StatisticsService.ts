import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DailyServiceUsage {
  id: number;
  serviceName: string;
  date: string; // ISO Date string
  usageCount: number;
}

export interface MonthlyServiceUsage {
  id: number;
  serviceName: string;
  month: string; // Format: YYYY-MM
  usageCount: number;
}

export interface DailyRevenue {
  id: number;
  serviceName: string;
  date: string; // ISO Date string
  revenueAmount: number;
}

export interface MonthlyRevenue {
  id: number;
  serviceName: string;
  month: string; // Format: YYYY-MM
  revenueAmount: number;
}

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private baseUrl = 'http://localhost:8090/api/statistics';  // Adjust the base URL as necessary

  constructor(private http: HttpClient) { }

  // Existing methods...

  getServiceUsageStatistics(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.baseUrl}/usage`);
  }

  getRevenueStatistics(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.baseUrl}/revenue`);
  }

  // New methods for aggregated statistics

  getDailyServiceUsage(): Observable<DailyServiceUsage[]> {
    return this.http.get<DailyServiceUsage[]>(`${this.baseUrl}/daily-service-usage`);
  }

  getMonthlyServiceUsage(): Observable<MonthlyServiceUsage[]> {
    return this.http.get<MonthlyServiceUsage[]>(`${this.baseUrl}/monthly-service-usage`);
  }

  getDailyRevenue(): Observable<DailyRevenue[]> {
    return this.http.get<DailyRevenue[]>(`${this.baseUrl}/daily-revenue`);
  }

  getMonthlyRevenue(): Observable<MonthlyRevenue[]> {
    return this.http.get<MonthlyRevenue[]>(`${this.baseUrl}/monthly-revenue`);
  }

  getUserUsageStatistics(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.baseUrl}/user-usage`);
  }

  getGroupUsageStatistics(): Observable<{ [key: number]: number }> {
    return this.http.get<{ [key: number]: number }>(`${this.baseUrl}/group-usage`);
  }

  getUserRevenueStatistics(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.baseUrl}/user-revenue`);
  }

  getGroupRevenueStatistics(): Observable<{ [key: number]: number }> {
    return this.http.get<{ [key: number]: number }>(`${this.baseUrl}/group-revenue`);
  }
}
