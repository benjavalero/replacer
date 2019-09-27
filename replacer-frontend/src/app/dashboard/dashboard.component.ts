import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';

import { AuthenticationService } from '../authentication/authentication.service';
import { User } from '../authentication/user.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: []
})
export class DashboardComponent implements OnInit {

  admin = false;

  constructor(private authenticationService: AuthenticationService, private titleService: Title) { }

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazador de la Wikipedia en español');
    if (this.authenticationService.user) {
      this.admin = this.authenticationService.user.admin;
    }

    this.authenticationService.userEvent.subscribe((user: User) => {
      this.admin = user.admin;
    });
  }

}
