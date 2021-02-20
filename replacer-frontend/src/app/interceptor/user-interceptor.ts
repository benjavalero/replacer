import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UserService, USER_PARAM } from '../user/user.service';

@Injectable()
export class UserInterceptor implements HttpInterceptor {
  constructor(private userService: UserService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const name = this.userService.userName;
    if (!req.params.has(USER_PARAM) && name) {
      return next.handle(req.clone({ params: req.params.append(USER_PARAM, name) }));
    } else {
      return next.handle(req);
    }
  }
}
