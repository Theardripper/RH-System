import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { Department, DepartmentRequest, PageParams, PageResponse } from '@core/models';

@Injectable({ providedIn: 'root' })
export class DepartmentService {

  private readonly url = `${environment.apiUrl}/departments`;

  constructor(private http: HttpClient) {}

  findAll(params: PageParams = {}): Observable<PageResponse<Department>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 10)
      .set('sort', params.sort ?? 'name');

    if (params.search) httpParams = httpParams.set('search', params.search);

    return this.http.get<PageResponse<Department>>(this.url, { params: httpParams });
  }

  findAllList(): Observable<Department[]> {
    return this.http.get<Department[]>(`${this.url}/all`);
  }

  findById(id: number): Observable<Department> {
    return this.http.get<Department>(`${this.url}/${id}`);
  }

  create(request: DepartmentRequest): Observable<Department> {
    return this.http.post<Department>(this.url, request);
  }

  update(id: number, request: DepartmentRequest): Observable<Department> {
    return this.http.put<Department>(`${this.url}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
