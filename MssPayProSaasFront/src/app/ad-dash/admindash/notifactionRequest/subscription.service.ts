import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {

  private baseUrl = 'http://localhost:8884/admin/subscriptions';

  constructor(private http: HttpClient) {}

  getSubscriptionStatus(serviceId: string, groupId: number): Observable<any> {
    const url = `${this.baseUrl}/status/${serviceId}`;
    const params = { groupId: groupId.toString() };

    console.log(`Making API call to: ${url} with groupId: ${groupId}`); // Log the API call

    return this.http.get<any>(url, { params }).pipe(
      tap(response => console.log('Subscription status response:', response)) // Log the response
    );
  }
}
