import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AlertService } from '../../shared/alert/alert.service';
import { LoginService } from '../authentication/login.service';
import { UserService } from '../user/user.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationGuard {
  constructor(
    private loginService: LoginService,
    private userService: UserService,
    private alertService: AlertService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.checkCanActivate(route, state);
  }

  private checkCanActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    // Clear all alerts
    this.alertService.clearAlertMessages();

    if (state.url === '/dashboard') {
      // Nothing to do
      return true;
    }

    if (this.userService.isValidUser()) {
      if (this.userService.hasRightsUser()) {
        return true;
      } else {
        console.log('Valid user with no rights. Redirect to Dashboard.');
        return this.router.parseUrl('dashboard');
      }
    } else {
      // Save redirect url so after authenticating we can move them back to the page they requested
      this.loginService.redirectPath = state.url;

      // Navigate to login page
      console.log('Not valid user. Save current route and redirect to Dashboard.');
      return this.router.parseUrl('dashboard');
    }
  }

  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.checkCanActivate(childRoute, state);
  }
}
