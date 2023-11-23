import { AsyncPipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { AlertComponent } from '../../../shared/alerts/alert-container/alert/alert.component';
import { UserLoginService } from '../../services/user-login.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [NgIf, AsyncPipe, AlertComponent],
  templateUrl: './login.component.html',
  styles: []
})
export class LoginComponent implements OnInit {
  authorizationUrl$!: Observable<string>;

  constructor(private loginService: UserLoginService) {}

  ngOnInit() {
    this.authorizationUrl$ = this.loginService.getAuthorizationUrl$();
  }
}
