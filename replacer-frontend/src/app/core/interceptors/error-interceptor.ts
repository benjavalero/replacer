import { HttpInterceptorFn, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AlertService } from '../../shared/alerts/alert.service';
import { UserConfigService } from '../services/user-config.service';
import { UserService } from '../services/user.service';

export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const alertService = inject(AlertService);
  const userService = inject(UserService);
  const userConfigService = inject(UserConfigService);

  return next(request).pipe(
    catchError((err) => {
      console.error('ERROR', err);
      console.error('ERROR STATUS', err.status);
      if (err.status == HttpStatusCode.Conflict) {
        alertService.addErrorMessage(
          'Esta página de Wikipedia ha sido editada por otra persona. Recargue para revisarla de nuevo.'
        );
      } else if (err.status == HttpStatusCode.Unauthorized) {
        // Clear session and reload the page
        userService.clearSession();
        userConfigService.clearConfig();
        window.location.reload();
      } else {
        // Generic error. We try to get it from the response in case of an exception from the server.
        alertService.addErrorMessage('Error: ' + (err.error?.message || err.message));
      }
      return throwError(() => err);
    })
  );
};
