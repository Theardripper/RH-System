import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { Employee, EmployeeRequest, PageParams, PageResponse } from '@core/models';

@Injectable({ providedIn: 'root' })
export class EmployeeService {

  private readonly url = `${environment.apiUrl}/employees`;

  constructor(private http: HttpClient) {}

  findAll(params: PageParams = {}): Observable<PageResponse<Employee>> {
    let httpParams = new HttpParams()
      .set('page',      params.page      ?? 0)
      .set('size',      params.size      ?? 10)
      .set('sort',      params.sort      ?? 'firstName')
      .set('direction', params.direction ?? 'asc');

    if (params.search)       httpParams = httpParams.set('search', params.search);
    if (params.departmentId) httpParams = httpParams.set('departmentId', params.departmentId);

    return this.http.get<PageResponse<Employee>>(this.url, { params: httpParams });
  }

  findById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.url}/${id}`);
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.url, request);
  }

  update(id: number, request: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.url}/${id}`, request);
  }

  deactivate(id: number): Observable<Employee> {
    return this.http.patch<Employee>(`${this.url}/${id}/deactivate`, {});
  }

  activate(id: number): Observable<Employee> {
    return this.http.patch<Employee>(`${this.url}/${id}/activate`, {});
  }
}
