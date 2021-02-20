import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from 'src/app/alert/alert.service';
import { AuthenticationService } from './authentication.service';
import { WikipediaUser } from './wikipedia-user.model';

@Component({
  selector: 'app-oauth',
  template: `
    <p>Comprobando autenticación…</p>
  `,
  styles: []
})
export class OAuthResponseComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private authenticationService: AuthenticationService,
    private router: Router,
    private alertService: AlertService
  ) {}

  ngOnInit(): void {
    // Manage return from authorization URL
    this.route.queryParams.subscribe((params) => {
      const oauthVerifier: string = params['oauth_verifier'];
      if (oauthVerifier) {
        this.authenticationService.loginUser$(oauthVerifier).subscribe(
          (user: WikipediaUser) => {
            this.router.navigate([this.authenticationService.redirectPath || 'dashboard']);
          },
          (err) => {
            this.alertService.addErrorMessage('Error al solicitar un Access Token del API de MediaWiki');
          }
        );
      } else {
        this.router.navigate(['dashboard']);
      }
    });
  }
}
