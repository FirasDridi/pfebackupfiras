import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private baseUrl = 'http://localhost:8884/admin';

  constructor(private http: HttpClient) { }

  getPendingRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/subscriptions/pending`);
  }

  approveRequest(requestId: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/subscriptions/approve/${requestId}`, {});
}

rejectRequest(requestId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/subscriptions/reject/${requestId}`, {});
}

}
