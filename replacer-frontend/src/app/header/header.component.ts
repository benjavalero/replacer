import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { User } from '../user/user.model';
import { Language } from '../user/language-model';
import { UserConfigService } from '../user/user-config.service';
import { UserService } from '../user/user.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  isNavCollapsed = true;
  user$: Observable<User>;
  lang$: Observable<Language>;

  constructor(private userService: UserService, private userConfigService: UserConfigService, private router: Router) {}

  ngOnInit(): void {
    this.user$ = this.userService.user$;
    this.lang$ = this.userConfigService.config$.pipe(map((config) => config.lang));
  }

  onSelectLang(lang: string) {
    // Enable language
    const language: Language = Language[lang];
    if (language) {
      this.userConfigService.lang = language;
      this.router.navigate(['']);
    }
  }

  onCloseSession() {
    // Clear session and reload the page
    this.userService.clearSession();
    this.router.navigate(['']);
  }
}
