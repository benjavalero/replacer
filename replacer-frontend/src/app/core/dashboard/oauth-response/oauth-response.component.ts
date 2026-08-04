import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '../../../shared/alerts/alert.service';
import { UserLoginService } from '../../services/user-login.service';

@Component({
  selector: 'app-oauth',
  template: ``,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OAuthResponseComponent {
  constructor() {
    const route = inject(ActivatedRoute);
    const loginService = inject(UserLoginService);
    const router = inject(Router);
    const alertService = inject(AlertService);

    alertService.addInfoMessage('Comprobando autenticación…');

    // Manage return from authorization URL
    route.queryParams.subscribe((params) => {
      const oauthVerifier: string = params['oauth_verifier'];
      if (oauthVerifier) {
        loginService.loginUser$(oauthVerifier).subscribe({
          next: () => {
            router.navigate([loginService.redirectPath || 'dashboard']);
          },
          error: () => {
            alertService.addErrorMessage('Error al solicitar un Access Token del API de MediaWiki');
          }
        });
      } else {
        console.log('Empty route but not back from authorization. Redirect to Dashboard.');
        router.navigate(['dashboard']);
      }
    });
  }
}
