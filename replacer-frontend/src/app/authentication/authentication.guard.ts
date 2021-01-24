import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthenticationService } from './authentication.service';
import { Language, LANG_PARAM } from './wikipedia-user.model';

@Injectable()
export class AuthenticationGuard implements CanActivate {
  constructor(private authenticationService: AuthenticationService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | Observable<boolean> | Promise<boolean> {
    // Enable language by param
    const lang: Language = Language[route.queryParamMap.get(LANG_PARAM)];
    if (lang) {
      this.authenticationService.lang = lang;
    }

    if (this.authenticationService.isAuthenticated()) {
      return true;
    } else {
      // Save redirect url so after authenticating we can move them back to the page they requested
      this.authenticationService.redirectPath = route.url.join('/');

      // Navigate to login page
      this.router.navigate(['']);

      return false;
    }
  }
}
