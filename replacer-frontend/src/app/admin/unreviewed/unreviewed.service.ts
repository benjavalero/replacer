import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageCount } from './page-count.model';

@Injectable({
  providedIn: 'root'
})
export class UnreviewedService {
  constructor(private httpClient: HttpClient) {}

  findPagesWithMostUnreviewedReplacements$(): Observable<PageCount[]> {
    return this.httpClient.get<PageCount[]>(`${environment.apiUrl}/replacement/page/count`);
  }
}
