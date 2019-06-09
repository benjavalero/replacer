import { Component, OnInit } from '@angular/core';

import { AuthenticationService } from '../authentication/authentication.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: []
})
export class HeaderComponent implements OnInit {
  isNavCollapsed = true;
  username: string;

  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit() {
    this.username = this.authenticationService.username;
    this.authenticationService.usernameEvent.subscribe((username: string) => this.username = username);
  }
}
