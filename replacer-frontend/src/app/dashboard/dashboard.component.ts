import { Component, OnInit } from '@angular/core';

import { AuthenticationService } from '../authentication/authentication.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: []
})
export class DashboardComponent implements OnInit {

  admin = false;

  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit() {
    this.admin = this.authenticationService.user.admin;
  }
}
