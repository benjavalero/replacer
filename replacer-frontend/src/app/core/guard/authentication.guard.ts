import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { AlertService } from '../../shared/alert/alert.service';
import { LoginService } from '../authentication/login.service';
import { UserService } from '../user/user.service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationGuard implements CanActivate, CanActivateChild {
  constructor(
    private loginService: LoginService,
    private userService: UserService,
    private alertService: AlertService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | Observable<boolean> | Promise<boolean> {
    return this.checkCanActivate(route, state);
  }

  private checkCanActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | Observable<boolean> | Promise<boolean> {
    // Clear all alerts
    this.alertService.clearAlertMessages();

    if (this.userService.isValidUser()) {
      if (!this.userService.hasRightsUser() && state.url !== '/dashboard') {
        this.router.navigate(['dashboard']);
      }
      return true;
    } else {
      // Save redirect url so after authenticating we can move them back to the page they requested
      this.loginService.redirectPath = state.url;

      // Navigate to login page
      this.router.navigate(['login']);

      return false;
    }
  }

  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | Observable<boolean> | Promise<boolean> {
    return this.checkCanActivate(childRoute, state);
  }
}
