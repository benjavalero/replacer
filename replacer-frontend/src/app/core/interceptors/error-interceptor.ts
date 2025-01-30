import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpStatusCode } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AlertService } from '../../shared/alerts/alert.service';
import { UserService } from '../services/user.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private alertService: AlertService,
    private userService: UserService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((err: any) => {
        console.error('ERROR', err);
        console.error('ERROR STATUS', err.status);
        if (err.status == HttpStatusCode.Conflict) {
          this.alertService.addErrorMessage(
            'Esta página de Wikipedia ha sido editada por otra persona. Recargue para revisarla de nuevo.'
          );
        } else if (err.status == HttpStatusCode.Unauthorized) {
          // Clear session and reload the page
          this.userService.clearSession();
          window.location.reload();
        } else {
          // Generic error. We try to get it from the response in case of an exception from the server.
          this.alertService.addErrorMessage('Error: ' + (err.error?.message || err.message));
        }
        return throwError(() => err);
      })
    );
  }
}
