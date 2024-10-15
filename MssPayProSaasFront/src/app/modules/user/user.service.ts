// src/app/modules/user/user.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UserGroupDTO } from '../group/group/UserGroupDTO';
import { UserDTO } from './user.dto';
import { GroupDto } from '../group/group.dto';
import { ServiceDto } from '../api-service/ServiceDto';
import { ServiceDetailsDto } from '../api-service/ServiceDetailsDto';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private baseUrl = 'http://localhost:8884/admin'; // Update URL accordingly
  private appUrl = 'http://localhost:8884';
  private apiurl = 'http://localhost:8081/service/tt';

  constructor(private http: HttpClient) {}

  getUserById(id: string): Observable<UserGroupDTO> {
    return this.http
      .get<UserGroupDTO>(`${this.baseUrl}/getUser/${id}`)
      .pipe(catchError(this.handleError));
  }

  createUser(user: UserGroupDTO): Observable<any> {
    console.log('Creating user with DTO:', user); // Log the DTO being sent
    return this.http
      .post<any>(`${this.baseUrl}/addUser`, user)
      .pipe(catchError(this.handleError));
  }

  updateUser(user: UserDTO): Observable<UserDTO> {
    return this.http
      .post<UserDTO>(`${this.baseUrl}/updateUser`, user)
      .pipe(catchError(this.handleError));
  }

  deleteUser(id: number): Observable<void> { // Changed id type to number
    if (id === undefined || id === null) {
      return throwError(() => new Error('User ID is undefined.'));
    }
    return this.http
      .delete<void>(`${this.baseUrl}/deleteUser/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    console.error('An error occurred:', error); // Log error for debugging
    let errorMessage = 'Something bad happened; please try again later.';
    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `An error occurred: ${error.error.message}`;
    } else {
      // Backend error
      errorMessage = `Backend returned code ${
        error.status
      }, body was: ${JSON.stringify(error.error)}`;
    }
    return throwError(() => new Error(errorMessage));
  }

  addUsersToGroup(groupName: string, users: UserGroupDTO[]): Observable<any> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http.post<any>(`${this.baseUrl}/addUsersToGroup?groupName=${groupName}`, users, { headers })
      .pipe(catchError(this.handleError));
  }

  // New method to fetch users with database ID
  getAllDbUsers(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(`${this.baseUrl}/getAllUsers`).pipe(
      catchError(this.handleError)
    );
  }

  getUserDetailsFromKeycloak(userId: string): Observable<UserDTO> {
    if (!userId) {
      return throwError(() => new Error('User ID is undefined.'));
    }
    return this.http.get<UserDTO>(`${this.baseUrl}/keycloak/user/${userId}`).pipe(
      catchError(this.handleError)
    );
  }

  getUserServices(userId: number): Observable<Set<string>> {
    return this.http.get<Set<string>>(`${this.baseUrl}/userServices/${userId}`);
  }

  addServiceToGroup(serviceId: string, groupId: string): Observable<void> {
    const url = `${this.baseUrl}/addGroupToService?serviceId=${serviceId}&groupId=${groupId}`;
    return this.http.post<void>(url, {});
  }

  getUserGroups(userId: number): Observable<UserGroupDTO[]> {
    return this.http.get<UserGroupDTO[]>(
      `${this.baseUrl}/user/${userId}/dbGroups`
    );
  }

  getUserDetails(
    userId: string
  ): Observable<{ user: UserDTO; groups: GroupDto[] }> {
    return this.http.get<{ user: UserDTO; groups: GroupDto[] }>(
      `${this.baseUrl}/user/${userId}/info`
    );
  }

  getServiceIdsByGroupId(groupId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.appUrl}/gets/${groupId}/servicesId`);
  }

  getServicesByIds(ids: string[]): Observable<ServiceDetailsDto[]> {
    const idsParam = ids.join(',');
    return this.http.get<ServiceDetailsDto[]>(
      `${this.apiurl}/gets/${idsParam}/services`
    );
  }
  updateConnectedUser(user: UserDTO): Observable<UserDTO> {
    return this.http
      .post<UserDTO>(`${this.baseUrl}/user/updateConnectedUser`, user)
      .pipe(catchError(this.handleError));
  }
  /**
   * Updates a user by their Keycloak ID.
   * @param id The Keycloak ID of the user.
   * @param user The updated user data.
   * @returns An Observable of the updated UserDTO.
   */
  updateUserById(id: string, user: UserDTO): Observable<UserDTO> {
    return this.http
      .post<UserDTO>(`${this.baseUrl}/user/updateUser/${id}`, user)
      .pipe(catchError(this.handleError));
  }
}
