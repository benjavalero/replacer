import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthenticationService } from './authentication.service';

@Injectable()
export class AuthenticationGuard implements CanActivate {

  constructor(private authenticationService: AuthenticationService, private router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Observable<boolean> | Promise<boolean> {
    if (this.authenticationService.isAuthenticated()) {
      return true;
    } else {
      // Save redirect url so after authing we can move them back to the page they requested
      this.authenticationService.redirectPath = route.url.join('/');

      // Navigate to login page
      this.router.navigate(['']);

      return false;
    }
  }

}
