import { Component, OnInit } from '@angular/core';

import { AuthenticationService } from '../authentication/authentication.service';
import { User } from '../authentication/user.model';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  isNavCollapsed = true;
  admin = false;
  username: string;

  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit() {
    if (this.authenticationService.user) {
      this.username = this.authenticationService.user.name;
      this.admin = this.authenticationService.user.admin;
    }

    this.authenticationService.userEvent.subscribe((user: User) => {
      this.username = user.name;
      this.admin = user.admin;
    });
  }
}
