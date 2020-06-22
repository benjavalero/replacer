import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { DumpIndexingStatus } from './dump-indexing-status.model';

@Injectable({
  providedIn: 'root',
})
export class DumpIndexingService {
  baseUrl = `${environment.apiUrl}/dump-indexing`;

  constructor(private httpClient: HttpClient) {}

  getDumpIndexingStatus(): Observable<DumpIndexingStatus> {
    return this.httpClient.get<DumpIndexingStatus>(`${this.baseUrl}/status`);
  }

  startDumpIndexing(): Observable<any> {
    return this.httpClient.post<any>(`${this.baseUrl}/start`, null);
  }
}
