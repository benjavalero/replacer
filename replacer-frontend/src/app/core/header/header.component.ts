import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Language } from '../user/language-model';
import { UserConfig } from '../user/user-config.model';
import { UserConfigService } from '../user/user-config.service';
import { User } from '../user/user.model';
import { UserService } from '../user/user.service';
import { ChangeLanguageComponent } from './change-language.component';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  isNavCollapsed = true;
  user$!: Observable<User>;
  lang$!: Observable<Language>;

  constructor(
    private userService: UserService,
    private userConfigService: UserConfigService,
    private router: Router,
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {
    this.user$ = this.userService.user$;
    this.lang$ = this.userConfigService.config$.pipe(map((config: UserConfig) => config.lang));
  }

  onSelectLang(lang: string) {
    // Enable language
    const language: Language = Language[lang as keyof typeof Language];
    if (language) {
      const modalRef = this.modalService.open(ChangeLanguageComponent);
      modalRef.result.then(
        (result) => {
          this.userConfigService.lang = language;
          this.closeSession();
        },
        (reason) => {
          // Nothing to do
        }
      );
    }
  }

  onCloseSession() {
    this.closeSession();
  }

  private closeSession(): void {
    // Clear session and reload the page
    this.userService.clearSession();
    this.router.navigate(['']);
  }
}
