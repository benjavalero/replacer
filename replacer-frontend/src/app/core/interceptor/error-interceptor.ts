import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpStatusCode } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AlertService } from '../../shared/alerts/alert.service';
import { UserService } from '../user/user.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private alertService: AlertService,
    private userService: UserService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((err: any) => {
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
          let serverMessage;
          if (err.error) {
            serverMessage = JSON.parse(err.error).message;
          }
          this.alertService.addErrorMessage('Error: ' + (serverMessage || err.message));
        }
        return throwError(() => err);
      })
    );
  }
}
