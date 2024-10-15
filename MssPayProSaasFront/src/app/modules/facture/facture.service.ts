import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FactureService {
  private apiUrl = 'http://localhost:8084/billing'; // Adjust this as per your environment

  constructor(private http: HttpClient) {}

  getUserInvoices(userId: string): Observable<any> { // This should be the Keycloak ID
    return this.http.get(`${this.apiUrl}/user/${userId}/invoices`);
  }

  getUserTotal(userId: string): Observable<number> { // This should be the Keycloak ID
    return this.http.get<number>(`${this.apiUrl}/user/${userId}/total`);
  }

  getGroupInvoices(groupId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/group/${groupId}/invoices`);
  }

  getGroupTotal(groupId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/group/${groupId}/total`);
  }

  generateInvoices(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/generate`, {});
  }
}
