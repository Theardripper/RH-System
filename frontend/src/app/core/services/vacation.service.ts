import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { VacationRequest, VacationRequestForm, VacationStatus, PageParams, PageResponse } from '@core/models';

@Injectable({ providedIn: 'root' })
export class VacationService {

  private readonly url = `${environment.apiUrl}/vacations`;

  constructor(private http: HttpClient) {}

  findAll(params: PageParams = {}): Observable<PageResponse<VacationRequest>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 10);

    if (params.status) httpParams = httpParams.set('status', params.status);

    return this.http.get<PageResponse<VacationRequest>>(this.url, { params: httpParams });
  }

  findByEmployee(employeeId: number, params: PageParams = {}): Observable<PageResponse<VacationRequest>> {
    const httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 10);

    return this.http.get<PageResponse<VacationRequest>>(
      `${this.url}/employee/${employeeId}`,
      { params: httpParams }
    );
  }

  findById(id: number): Observable<VacationRequest> {
    return this.http.get<VacationRequest>(`${this.url}/${id}`);
  }

  countPending(): Observable<number> {
    return this.http.get<number>(`${this.url}/pending/count`);
  }

  create(employeeId: number, form: VacationRequestForm): Observable<VacationRequest> {
    return this.http.post<VacationRequest>(`${this.url}/employee/${employeeId}`, form);
  }

  approve(id: number): Observable<VacationRequest> {
    return this.http.patch<VacationRequest>(`${this.url}/${id}/approve`, {});
  }

  reject(id: number): Observable<VacationRequest> {
    return this.http.patch<VacationRequest>(`${this.url}/${id}/reject`, {});
  }

  cancel(id: number): Observable<VacationRequest> {
    return this.http.patch<VacationRequest>(`${this.url}/${id}/cancel`, {});
  }
}
