import { Component, OnInit, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { UserService } from '../services/user.service';
import { LoginComponent } from './login/login.component';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, AlertComponent, LoginComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: []
})
export class DashboardComponent implements OnInit {
  private userService = inject(UserService);
  private titleService = inject(Title);

  isValidUser = this.userService.isValidUser;
  hasRightsUser = this.userService.hasRightsUser;

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazador de la Wikipedia');
  }
}
