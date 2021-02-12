import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthenticationService } from './authentication.service';
import { USER_PARAM } from './wikipedia-user.model';

@Injectable()
export class UserInterceptor implements HttpInterceptor {
  constructor(private authenticationService: AuthenticationService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!req.params.has(USER_PARAM) && this.authenticationService.user) {
      return next.handle(req.clone({ params: req.params.append(USER_PARAM, this.authenticationService.user.name) }));
    } else {
      return next.handle(req);
    }
  }
}
