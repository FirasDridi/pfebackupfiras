import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import Keycloak, { KeycloakInstance } from 'keycloak-js';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class KeycloakService {
  private keycloakInstance: KeycloakInstance;

  constructor(private http: HttpClient) {
    this.keycloakInstance = new Keycloak({
      url: 'http://localhost:8088', // Ensure this URL is correct
      realm: 'mss-authent',
      clientId: 'frontendfiras',
    });
  }

  init(): Promise<void> {
    return this.keycloakInstance
      .init({
        onLoad: 'login-required',
        checkLoginIframe: true,
      })
      .then((authenticated) => {
        console.log(authenticated ? 'Authenticated' : 'Not Authenticated');
        if (!authenticated) {
          this.keycloakInstance.login();
        }
      })
      .catch((err) => {
        console.error('Keycloak initialization failed', err);
      });
  }

  login(): void {
    this.keycloakInstance.login();
  }

  logout(): void {
    this.keycloakInstance.logout({ redirectUri: window.location.origin });
  }

  isLoggedIn(): boolean {
    return !!this.keycloakInstance.token;
  }

  getToken(): Promise<string> {
    return this.keycloakInstance
      .updateToken(10)
      .then(() => {
        return this.keycloakInstance.token || ''; // Return an empty string if token is undefined
      })
      .catch(() => {
        return ''; // Return an empty string if there's an error updating the token
      });
  }

  accountManagement(): void {
    this.keycloakInstance.accountManagement();
  }

  hasRole(role: string): boolean {
    return (
      this.keycloakInstance.tokenParsed?.realm_access?.roles.includes(role) ||
      false
    );
  }

  getUserProfile(): Promise<any> {
    return this.keycloakInstance.loadUserProfile();
  }

  getKeycloakInstance(): KeycloakInstance {
    return this.keycloakInstance;
  }

  getUserId(): string | undefined {
    return this.keycloakInstance.tokenParsed?.sub;
  }

  assignRoleToUser(userId: string, roleId: string): Observable<any> {
    if (!userId) {
      return throwError('User ID is undefined'); // Return an observable with an error if user ID is undefined
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.keycloakInstance.token}`,
      'Content-Type': 'application/json',
    });

    const roleRepresentation = {
      id: roleId,
      name: 'superuser' // Ensure this matches the role name in Keycloak
    };

    return this.http.post(
      `http://localhost:8088/admin/realms/mss-authent/users/${userId}/role-mappings/realm`,
      [roleRepresentation],
      { headers }
    ).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    console.error('An error occurred:', error.message);
    return throwError('Something bad happened; please try again later.');
  }
}
