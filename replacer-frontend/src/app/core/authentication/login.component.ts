import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginService } from './login.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styles: []
})
export class LoginComponent implements OnInit {
  authorizationUrl$!: Observable<string>;

  constructor(private loginService: LoginService) {}

  ngOnInit() {
    this.authorizationUrl$ = this.loginService.getAuthenticationUrl$();
  }
}
