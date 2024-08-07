import { HttpClient, HttpParams } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Observable } from 'rxjs';
import { LoggerService } from './helpers/logger';

const log = new LoggerService();
export class SimpleBaseController<T, I, O> {
  endpointService: string = '';
  http: HttpClient;
  constructor(injector: Injector) {
    this.http = injector.get(HttpClient);
  }

  findDtoByUuid(id: any): Observable<O> {
    log.info('findDtoByUuid');
    const url = `${this.endpointService}/fetch/${id}`;
    return this.http.get<O>(url);
  }
  findAllDtos(
    pagination: boolean,
    page: number,
    pageSize: number
  ): Observable<any[]> {
    log.info('getAfindAllDtosll');
    const url = `${this.endpointService}/list`;
    if (pagination) {
      return this.findAllWithPagination(page, pageSize);
    }
    return this.findAll();
  }

  findAllWithPagination(page: number, pageSize: number) {
    const url = `${this.endpointService}/list`;
    let queryParams = new HttpParams();
    queryParams = queryParams.append('page', page.toString()); // Convert to string
    queryParams = queryParams.append('size', pageSize.toString()); // Convert to string
    return this.http.get<any[]>(url, { params: queryParams });
  }

  findAll() {
    const url = `${this.endpointService}/getAll`;
    return this.http.get<O[]>(url);
  }
  save(data: I): Observable<O> {
    log.info('save');
    return this.http.post<O>(this.endpointService, data);
  }
  UpdateByUuid(id: string, data: I): Observable<O> {
    log.info('UpdateByUuid');
    const url = `${this.endpointService}/uuid/${id}`;
    return this.http.put<O>(url, data);
  }

  deleteByUuid(id: string): Observable<any> {
    log.info('deleteByUuid');
    const url = `${this.endpointService}/uuid/${id}`;
    return this.http.delete<any>(url);
  }
}
