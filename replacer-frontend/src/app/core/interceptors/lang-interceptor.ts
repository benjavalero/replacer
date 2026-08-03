import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { UserConfigService } from '../services/user-config.service';

export const langInterceptor: HttpInterceptorFn = (req, next) => {
  const lang = inject(UserConfigService).lang;

  return next(
    req.clone({
      headers: req.headers.append('Accept-Language', lang()),
      withCredentials: !environment.production
    })
  );
};
