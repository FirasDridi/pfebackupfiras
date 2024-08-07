import { Injectable, Injector } from '@angular/core';
import { Observable } from 'rxjs';
import { SimpleBaseController } from '../../base/base.service';
import { APP_CONFIG } from '../../base/config/app.config';
import { ServiceDto } from './ServiceDto';
import { HttpClient } from '@angular/common/http';
import { ServiceDetailsDto } from './ServiceDetailsDto';

@Injectable({
  providedIn: 'root',
})
export class ServiceUsageService extends SimpleBaseController<
  ServiceDto,
  ServiceDto,
  ServiceDto
> {
  private baseUrl = 'http://localhost:8081/service/api/services';


  constructor(private injector: Injector, ) {
    super(injector);
    this.endpointService = APP_CONFIG.apiBaseUrl + '/services';
  }

  getAllServices(pageIndex: number, pageSize: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/list?page=${pageIndex}&size=${pageSize}`);
  }


  getServiceById(id: string): Observable<ServiceDto> {
    return this.http.get<ServiceDto>(`${this.baseUrl}/fetch/${id}`);
  }

  createService(service: ServiceDto): Observable<ServiceDto> {
    return this.http.post<ServiceDto>(`${this.baseUrl}`, service);
  }

  updateService(id: string, service: ServiceDto): Observable<ServiceDto> {
    return this.http.put<ServiceDto>(`${this.baseUrl}/uuid/${id}`, service);
  }

  deleteService(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/uuid/${id}`);
  }

  activateService(serviceUsageId: string): Observable<string> {
    return this.http.patch<string>(
      `${this.baseUrl}/${serviceUsageId}/activate`,
      null
    );
  }

  deactivateService(serviceUsageId: string): Observable<string> {
    return this.http.patch<string>(
      `${this.baseUrl}/${serviceUsageId}/deactivate`,
      null
    );
  }

  performAdvancedSearch(searchData: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/advanced/search`, searchData);
  }

  existsByName(name: string): Observable<boolean> {
    return this.http.get<boolean>(
      `${this.baseUrl}/check-Name-exists?name=${name}`
    );
  }

  getServicesWithDetails(): Observable<ServiceDetailsDto[]> {
    return this.http.get<ServiceDetailsDto[]>(`${this.baseUrl}/listWithDetails`);
  }

  getUserApis(): Observable<ServiceDto[]> {
    return this.http.get<ServiceDto[]>(`${this.baseUrl}/user-apis`);
  }

  useService(groupId: number, userId: number, serviceName: string, serviceId: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/useService`, null, {
      params: {
        groupId: groupId.toString(),
        userId: userId.toString(),
        serviceName,
        serviceId
      }
    });
  }
}
