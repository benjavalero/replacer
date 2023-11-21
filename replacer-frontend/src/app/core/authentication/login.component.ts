import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { LoginService } from './login.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, AlertComponent],
  templateUrl: './login.component.html',
  styles: []
})
export class LoginComponent implements OnInit {
  authorizationUrl$!: Observable<string>;

  constructor(private loginService: LoginService) {}

  ngOnInit() {
    this.authorizationUrl$ = this.loginService.getAuthorizationUrl$();
  }
}
