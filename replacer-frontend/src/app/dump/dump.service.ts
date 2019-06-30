import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { DumpStatus } from './dump-status.model';

@Injectable({
  providedIn: 'root'
})
export class DumpService {

  constructor(private httpClient: HttpClient) { }

  findDumpStatus(): Observable<DumpStatus> {
    return this.httpClient.get<DumpStatus>(`${environment.apiUrl}/dump/`);
  }

  runIndexation(forceProcess: boolean): Observable<any> {
    const force = forceProcess ? 'force' : '';
    return this.httpClient.post<any>(`${environment.apiUrl}/dump/${force}`, null);
  }

}
