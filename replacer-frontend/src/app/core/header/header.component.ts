import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { NgbCollapseModule, NgbDropdownModule, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { User } from '../../api/models/user';
import { Language } from '../user/language.model';
import { UserConfig } from '../user/user-config.model';
import { UserConfigService } from '../user/user-config.service';
import { UserService } from '../user/user.service';
import { ChangeLanguageComponent } from './change-language.component';

@Component({
  standalone: true,
  selector: 'app-header',
  imports: [CommonModule, RouterModule, NgbCollapseModule, NgbDropdownModule, ChangeLanguageComponent],
  templateUrl: './header.component.html',
  styleUrls: []
})
export class HeaderComponent implements OnInit {
  isNavCollapsed = true;
  user$!: Observable<User | null>;
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
    const language: Language = lang as Language;
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
    console.log('Close session. Discard current user and redirect to Dashboard.');
    this.router.navigate(['dashboard']);
  }
}
