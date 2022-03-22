import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PublicIp } from './public-ip/public-ip.model';

@Injectable()
export class AdminService {
  private readonly baseUrl = `${environment.apiUrl}/admin`;

  constructor(private httpClient: HttpClient) {}

  getPublicIp$(): Observable<PublicIp> {
    return this.httpClient.get<PublicIp>(`${this.baseUrl}/public-ip`);
  }
}
