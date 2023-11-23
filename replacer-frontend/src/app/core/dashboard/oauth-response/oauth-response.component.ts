import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from 'src/app/shared/alerts/alert.service';
import { UserLoginService } from '../../services/user-login.service';

@Component({
  standalone: true,
  selector: 'app-oauth',
  template: ``,
  styles: []
})
export class OAuthResponseComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private loginService: UserLoginService,
    private router: Router,
    private alertService: AlertService
  ) {}

  ngOnInit(): void {
    this.alertService.addInfoMessage('Comprobando autenticación…');

    // Manage return from authorization URL
    this.route.queryParams.subscribe((params) => {
      const oauthVerifier: string = params['oauth_verifier'];
      if (oauthVerifier) {
        this.loginService.loginUser$(oauthVerifier).subscribe({
          next: () => {
            this.router.navigate([this.loginService.redirectPath || 'dashboard']);
          },
          error: () => {
            this.alertService.addErrorMessage('Error al solicitar un Access Token del API de MediaWiki');
          }
        });
      } else {
        console.log('Empty route but not back from authorization. Redirect to Dashboard.');
        this.router.navigate(['dashboard']);
      }
    });
  }
}
