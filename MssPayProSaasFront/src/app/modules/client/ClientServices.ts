  import { Injectable, Injector } from '@angular/core';
  import { HttpClient } from '@angular/common/http';
  import { Observable } from 'rxjs';
  import { ClientDto } from './ClientDto';
  import { SimpleBaseController } from '../../base/base.service';
  import { APP_CONFIG } from '../../base/config/app.config';

  @Injectable({
    providedIn: 'root',
  })
  export class ClientService extends SimpleBaseController<
    ClientDto,
    ClientDto,
    ClientDto
  > {
    private baseUrl = 'http://localhost:8083/Client/api/client'; // Update the base URL according to your backend API

    constructor(private injector: Injector) {
      super(injector);
      this.endpointService = APP_CONFIG.clientBaseUrl + '/client';
    }
    getAllClients(): Observable<ClientDto[]> {
      return this.http.get<ClientDto[]>(`${this.baseUrl}/list`);
    }

    getClientById(id: string): Observable<ClientDto> {
      return this.http.get<ClientDto>(`${this.baseUrl}/fetch/${id}`);
    }

    createClient(client: ClientDto): Observable<ClientDto> {
      return this.http.post<ClientDto>(`${this.baseUrl}`, client);
    }

    updateClient(id: string, client: ClientDto): Observable<ClientDto> {
      return this.http.put<ClientDto>(`${this.baseUrl}/uuid/${id}`, client);
    }

    deleteClient(id: string): Observable<void> {
      return this.http.delete<void>(`${this.baseUrl}/uuid/${id}`);
    }

    activateClient(clientId: string): Observable<string> {
      return this.http.patch<string>(
        `${this.baseUrl}/${clientId}/activate`,
        null
      );
    }

    deactivateClient(clientId: string): Observable<string> {
      return this.http.patch<string>(
        `${this.baseUrl}/${clientId}/deactivate`,
        null
      );
    }
    performAdvancedSearch(searchData: any): Observable<any> {
      return this.http.post<any>(`${this.baseUrl}/advanced/search`, searchData);
    }
  }
