import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { AuthenticationService } from '../authentication/authentication.service';
import { WikipediaUser } from '../authentication/wikipedia-user.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: []
})
export class DashboardComponent implements OnInit {
  admin = false;

  constructor(private authenticationService: AuthenticationService, private titleService: Title) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazador de la Wikipedia en español');

    this.authenticationService.user$.subscribe((user: WikipediaUser) => {
      this.admin = user.admin;
    });
  }
}
