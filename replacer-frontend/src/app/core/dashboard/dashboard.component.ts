import { NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { UserService } from '../services/user.service';
import { LoginComponent } from './login/login.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [NgIf, RouterLink, AlertComponent, LoginComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: []
})
export class DashboardComponent implements OnInit {
  isValidUser = this.userService.isValidUser;
  hasRightsUser = this.userService.hasRightsUser;

  constructor(
    private userService: UserService,
    private titleService: Title
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazador de la Wikipedia');
  }
}
