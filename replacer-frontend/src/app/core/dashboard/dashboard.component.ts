import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { LoginComponent } from '../authentication/login.component';
import { UserService } from '../user/user.service';

@Component({
  standalone: true,
  selector: 'app-dashboard',
  imports: [CommonModule, RouterModule, AlertComponent, LoginComponent],
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
