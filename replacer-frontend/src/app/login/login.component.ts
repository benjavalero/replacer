import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { AuthenticationService } from '../authentication/authentication.service';
import { RequestToken } from '../authentication/request-token.model';
import { AccessToken } from '../authentication/access-token.model';
import { AlertService } from '../alert/alert.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styles: []
})
export class LoginComponent implements OnInit {

  authorizationUrl: string;

  constructor(private route: ActivatedRoute, private authenticationService: AuthenticationService, private router: Router,
    private alertService: AlertService) { }

  ngOnInit() {
    this.alertService.addInfoMessage('Comprobando autenticación…');

    // Manage return from authorization URL
    this.route.queryParams.subscribe(params => {
      /* tslint:disable: no-string-literal */
      const oauthVerifier: string = params['oauth_verifier'];
      if (oauthVerifier) {
        this.authenticationService.generateAccessToken(oauthVerifier).subscribe((token: AccessToken) => {
          // Save access token to further use in Wikipedia requests
          this.authenticationService.accessToken = token;

          // Remove request token as it is no longer needed
          this.authenticationService.requestToken = null;

          // Redirect to previous URL
          this.alertService.clearAlertMessages();
          this.router.navigate([this.authenticationService.redirectPath || 'dashboard']);
          this.authenticationService.redirectPath = null;
        }, (err) => {
          this.alertService.addErrorMessage('Error al solicitar un Access Token del API de MediaWiki');
        });
      } else {
        if (this.authenticationService.isAuthenticated()) {
          this.alertService.clearAlertMessages();
          this.router.navigate(['dashboard']);
        } else {
          this.generateAuthenticationUrl();
        }
      }
    });
  }

  private generateAuthenticationUrl() {
    // We need to generate first a request token
    this.authenticationService.generateRequestToken().subscribe((requestToken: RequestToken) => {
      // We keep the request token for further use on verification
      this.authenticationService.requestToken = requestToken;

      this.authorizationUrl = requestToken.url;
      this.alertService.clearAlertMessages();
    }, (err) => {
      this.alertService.addErrorMessage('Error al solicitar un Request Token del API de MediaWiki');
    });
  }

}
