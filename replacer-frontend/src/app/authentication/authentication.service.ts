import { Injectable, Output, EventEmitter } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { RequestToken } from './request-token.model';
import { AccessToken } from './access-token.model';
import { User } from './user.model';
import { AlertService } from '../alert/alert.service';
import { VerificationToken } from './verification-token.model';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  @Output() userEvent: EventEmitter<User> = new EventEmitter();

  constructor(private httpClient: HttpClient, private alertService: AlertService) { }

  isAuthenticated(): boolean {
    return this.accessToken !== null;
  }

  generateRequestToken(): Observable<RequestToken> {
    return this.httpClient.get<RequestToken>(`${environment.apiUrl}/authentication/requestToken`);
  }

  generateAccessToken(verificationToken: string): Observable<AccessToken> {
    const verificationTokenDto = new VerificationToken();
    verificationTokenDto.requestToken = this.requestToken;
    verificationTokenDto.verificationToken = verificationToken;

    return this.httpClient.post<AccessToken>(`${environment.apiUrl}/authentication/accessToken`, verificationTokenDto);
  }

  clearSession(): void {
    this.accessToken = null;
    this.user = null;
  }

  get redirectPath(): string {
    return localStorage.getItem('redirectPath');
  }

  set redirectPath(path: string) {
    if (path) {
      localStorage.setItem('redirectPath', path);
    } else {
      localStorage.removeItem('redirectPath');
    }
  }

  get requestToken(): RequestToken {
    return JSON.parse(localStorage.getItem('requestToken'));
  }

  set requestToken(token: RequestToken) {
    if (token) {
      localStorage.setItem('requestToken', JSON.stringify(token));
    } else {
      localStorage.removeItem('requestToken');
    }
  }

  get accessToken(): AccessToken {
    return JSON.parse(localStorage.getItem('accessToken'));
  }

  set accessToken(token: AccessToken) {
    if (token) {
      localStorage.setItem('accessToken', JSON.stringify(token));
      this.findUserName();
    } else {
      localStorage.removeItem('accessToken');
    }
  }

  private findUserName(): void {
    this.httpClient.post<User>(`${environment.apiUrl}/wikipedia/username`, this.accessToken)
      .subscribe((user: User) => {
        this.user = user;
      }, (err) => {
        this.alertService.addErrorMessage('Error al buscar el nombre del usuario en sesión');
      });
  }

  get user(): User {
    return JSON.parse(localStorage.getItem('user'));
  }

  set user(user: User) {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    } else {
      localStorage.removeItem('user');
    }
    this.userEvent.emit(user);
  }

}
