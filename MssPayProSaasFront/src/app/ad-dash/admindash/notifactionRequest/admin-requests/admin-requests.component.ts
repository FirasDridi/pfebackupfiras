import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-admin-requests',
  templateUrl: './admin-requests.component.html',
  styleUrls: ['./admin-requests.component.css']
})
export class AdminRequestsComponent implements OnInit {
  requests: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(): void {
    this.http.get<any[]>('http://localhost:8884/admin/subscriptions/pending').subscribe(data => {
      this.requests = data;
    });
  }

  approveRequest(requestId: number): void {
    this.http.post(`http://localhost:8884/admin/subscriptions/approve/${requestId}`, {}).subscribe(() => {
      this.loadRequests();
    });
  }

  rejectRequest(requestId: number): void {
    this.http.post(`http://localhost:8884/admin/subscriptions/reject/${requestId}`, {}).subscribe(() => {
      this.loadRequests();
    });
  }
}
