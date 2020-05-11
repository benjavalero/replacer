import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthenticationService } from './authentication.service';
import { LANG_PARAM } from './wikipedia-user.model';

@Injectable()
export class LangInterceptor implements HttpInterceptor {

  constructor(private authenticationService: AuthenticationService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!req.params.has(LANG_PARAM)) {
      return next.handle(req.clone({ params: req.params.append('lang', this.authenticationService.lang) }));
    } else {
      return next.handle(req);
    }
  }
}
