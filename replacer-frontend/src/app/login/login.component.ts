import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthenticationService } from '../authentication/authentication.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styles: []
})
export class LoginComponent implements OnInit {
  authorizationUrl$: Observable<string>;

  constructor(private authenticationService: AuthenticationService) {}

  ngOnInit() {
    this.authorizationUrl$ = this.authenticationService.getAuthenticationUrl$();
  }
}
