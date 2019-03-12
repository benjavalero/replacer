import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, Route } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../environments/environment';

@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private httpClient: HttpClient, private _router: Router) {
  }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
    return this.httpClient
      .get<boolean>(`${environment.apiUrl}/authenticated`).pipe(
        map((res: boolean) => {
          if (res) {
            return true;
          } else {
            // Navigate to login page
            this._router.navigate(['/login']);
            // TODO : Save redirect url so after authing we can move them back to the page they requested
            return false;
          }
        }));
  }

}
