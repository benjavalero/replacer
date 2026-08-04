import { AsyncPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AlertComponent } from '../../../shared/alerts/alert-container/alert/alert.component';
import { UserLoginService } from '../../services/user-login.service';

@Component({
  selector: 'app-login',
  imports: [AsyncPipe, AlertComponent],
  templateUrl: './login.component.html',
  styles: []
})
export class LoginComponent implements OnInit {
  private loginService = inject(UserLoginService);

  authorizationUrl$!: Observable<string>;

  ngOnInit() {
    this.authorizationUrl$ = this.loginService.getAuthorizationUrl$();
  }
}
