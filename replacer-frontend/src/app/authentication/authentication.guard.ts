import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { Language } from '../user/language-model';
import { LANG_PARAM, UserConfigService } from '../user/user-config.service';
import { UserService } from '../user/user.service';
import { AuthenticationService } from './authentication.service';

@Injectable()
export class AuthenticationGuard implements CanActivate {
  constructor(
    private authenticationService: AuthenticationService,
    private userService: UserService,
    private userConfigService: UserConfigService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | Observable<boolean> | Promise<boolean> {
    // Enable language by param
    const lang: Language = Language[route.queryParamMap.get(LANG_PARAM)];
    if (lang) {
      this.userConfigService.lang = lang;
    }

    if (this.userService.isValidUser()) {
      return true;
    } else {
      // Save redirect url so after authenticating we can move them back to the page they requested
      this.authenticationService.redirectPath = route.url.join('/');

      // Navigate to login page
      this.router.navigate(['login']);

      return false;
    }
  }
}
