import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LANG_PARAM, UserConfigService } from '../user/user-config.service';

@Injectable()
export class LangInterceptor implements HttpInterceptor {
  constructor(private userConfigService: UserConfigService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!req.params.has(LANG_PARAM)) {
      return next.handle(req.clone({ params: req.params.append(LANG_PARAM, this.userConfigService.lang) }));
    } else {
      return next.handle(req);
    }
  }
}
