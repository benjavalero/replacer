import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { AlertService } from '../../shared/alerts/alert.service';
import { LoginService } from '../authentication/login.service';
import { UserService } from '../user/user.service';

export const authenticationGuard = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const alertService: AlertService = inject(AlertService);
  const userService: UserService = inject(UserService);
  const loginService: LoginService = inject(LoginService);
  const router: Router = inject(Router);

  // Clear all alerts
  alertService.clearAlertMessages();

  if (state.url === '/dashboard') {
    // Nothing to do
    return true;
  }

  if (userService.isValidUser()) {
    if (userService.hasRightsUser()) {
      return true;
    } else {
      console.log('Valid user with no rights. Redirect to Dashboard.');
      return router.parseUrl('dashboard');
    }
  } else {
    // Save redirect url so after authenticating we can move them back to the page they requested
    loginService.redirectPath = state.url;

    // Navigate to login page
    console.log('Not valid user. Save current route and redirect to Dashboard.');
    return router.parseUrl('dashboard');
  }
};
