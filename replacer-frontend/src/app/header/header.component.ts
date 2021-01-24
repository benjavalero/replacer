import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../authentication/authentication.service';
import { Language, WikipediaUser } from '../authentication/wikipedia-user.model';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  isNavCollapsed = true;
  user: WikipediaUser;
  lang: Language;

  constructor(private authenticationService: AuthenticationService, private router: Router) {}

  ngOnInit(): void {
    this.authenticationService.user$.subscribe((user: WikipediaUser) => {
      this.user = user;
    });

    this.authenticationService.lang$.subscribe((lang: Language) => {
      this.lang = lang;
    });
  }

  onSelectLang(lang: string) {
    // Enable language
    const language: Language = Language[lang];
    if (language) {
      this.authenticationService.lang = language;
      this.router.navigate(['']);
    }
  }

  onCloseSession() {
    // Clear session and reload the page
    this.authenticationService.clearSession();
    this.router.navigate(['']);
  }
}
