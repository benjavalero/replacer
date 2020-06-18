import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AuthenticationService } from '../authentication/authentication.service';
import { WikipediaUser, Language } from '../authentication/wikipedia-user.model';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  isNavCollapsed = true;
  user: WikipediaUser;
  defaultLang = Language.es;

  constructor(private authenticationService: AuthenticationService, private router: Router) { }

  ngOnInit() {
    if (this.authenticationService.user) {
      this.user = this.authenticationService.user;
    }

    this.authenticationService.userEvent.subscribe((user: WikipediaUser) => {
      this.user = user;
    });
  }

  onSelectLang(lang: string) {
    // Enable language
    const language: Language = Language[lang];
    if (lang) {
      this.authenticationService.lang = language;
    }
  }

  onCloseSession() {
      // Clear session and reload the page
      this.authenticationService.clearSession();
      this.router.navigate(['']);
  }
}
