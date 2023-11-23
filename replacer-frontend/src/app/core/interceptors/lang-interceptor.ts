import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserConfigService } from '../services/user-config.service';

@Injectable()
export class LangInterceptor implements HttpInterceptor {
  lang = this.userConfigService.lang;

  constructor(private userConfigService: UserConfigService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(
      req.clone({
        headers: req.headers.append('Accept-Language', this.lang()),
        withCredentials: !environment.production
      })
    );
  }
}
