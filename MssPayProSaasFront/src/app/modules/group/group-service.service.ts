import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserGroupDTO } from './group/UserGroupDTO';
import { ServiceDto } from '../api-service/ServiceDto';

@Injectable({
  providedIn: 'root',
})
export class GroupService {
  private baseUrl = 'http://localhost:8884/api/v1/groups';

  constructor(private http: HttpClient) {}

  getAllGroups(): Observable<UserGroupDTO[]> {
    return this.http.get<UserGroupDTO[]>(`${this.baseUrl}/list`);
  }

  getGroupById(id: string): Observable<UserGroupDTO> {
    return this.http.get<UserGroupDTO>(`${this.baseUrl}/details/${id}`);
  }

  createGroup(group: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, group);
  }

  updateGroup(id: string, group: UserGroupDTO): Observable<UserGroupDTO> {
    return this.http.put<UserGroupDTO>(`${this.baseUrl}/update/${id}`, group);
  }

  deleteGroup(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete/${id}`);
  }

  getGroupUsers(groupId: string): Observable<UserGroupDTO[]> {
    return this.http.get<UserGroupDTO[]>(`${this.baseUrl}/${groupId}/users`);
  }

  deleteAllAccessTokens(groupId: string): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${groupId}/access-tokens`);
  }


  getGroupApis(groupId: string): Observable<ServiceDto[]> {
    return this.http.get<ServiceDto[]>(`${this.baseUrl}/${groupId}/apis`);
  }
  addServiceToGroup(serviceId: string, groupId: string): Observable<void> {
    const url = `${this.baseUrl}/addGroupToService?serviceId=${serviceId}&groupId=${groupId}`;
    return this.http.post<void>(url, {});
  }
}
