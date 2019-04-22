import { Component, OnInit } from '@angular/core';

import { AuthenticationService } from '../authentication/authentication.service';
import { OauthToken } from '../authentication/oauth-token';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styles: []
})
export class LoginComponent implements OnInit {

  authorizationUrl: string;

  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit() {
    // We need to generate first a request token
    this.authenticationService.generateRequestToken().subscribe(
      (token: OauthToken) => {
        // We keep the request token for further use on verification
        this.authenticationService.requestToken = token;

        // Retrieve the authorization URL to redirect
        this.authenticationService.generateAuthorizationUrl(token).subscribe(
          (url: string) => {
            this.authorizationUrl = url;
          }
        );
      }
    );
  }

}
