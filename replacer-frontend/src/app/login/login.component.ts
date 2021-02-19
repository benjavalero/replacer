import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '../alert/alert.service';
import { AuthenticationService } from '../authentication/authentication.service';
import { WikipediaUser } from '../authentication/wikipedia-user.model';
import { UserService } from '../user/user.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styles: []
})
export class LoginComponent implements OnInit {
  authorizationUrl: string;

  constructor(
    private route: ActivatedRoute,
    private authenticationService: AuthenticationService,
    private userService: UserService,
    private router: Router,
    private alertService: AlertService
  ) {}

  ngOnInit() {
    this.alertService.addInfoMessage('Comprobando autenticación…');

    // Manage return from authorization URL
    this.route.queryParams.subscribe((params) => {
      const oauthVerifier: string = params['oauth_verifier'];
      if (oauthVerifier) {
        this.authenticationService.loginUser$(oauthVerifier).subscribe(
          (user: WikipediaUser) => {
            // Redirect to previous URL
            this.alertService.clearAlertMessages();
            this.router.navigate([this.authenticationService.redirectPath || 'dashboard']);
            this.authenticationService.redirectPath = null;
          },
          (err) => {
            this.alertService.addErrorMessage('Error al solicitar un Access Token del API de MediaWiki');
          }
        );
      } else {
        if (this.userService.isValidUser()) {
          this.alertService.clearAlertMessages();
          this.router.navigate(['dashboard']);
        } else {
          this.authenticationService.getAuthenticationUrl$().subscribe((url: string) => {
            this.alertService.clearAlertMessages();
            this.authorizationUrl = url;
          });
        }
      }
    });
  }
}
