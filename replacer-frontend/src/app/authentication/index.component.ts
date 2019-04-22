import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthenticationService } from './authentication.service';

import { OauthToken } from './oauth-token';

@Component({
  selector: 'app-index',
  template: '<p>Completando autenticación…</p>',
  styles: []
})
export class IndexComponent implements OnInit {

  constructor(private route: ActivatedRoute, private httpClient: HttpClient, private authenticationService: AuthenticationService, private router: Router) { }

  ngOnInit() {
    this.route
      .queryParams
      .subscribe(params => {
        const oauthVerifier: string = params['oauth_verifier'];
        if (oauthVerifier) {
          this.authenticationService.generateAccessToken(oauthVerifier).subscribe(
            (token: OauthToken) => {
              // Save access token to further use in Wikipedia requests
              this.authenticationService.accessToken = token;

              // Remove request token as it is no longer needed
              this.authenticationService.requestToken = null;

              // Redirect to previous URL
              this.router.navigate([this.authenticationService.redirectPath]);
              this.authenticationService.redirectPath = null;
            }
          );
        } else {
          this.router.navigate(['dashboard']);
        }
      });
  }

}
