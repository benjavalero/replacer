import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { NgbCollapseModule, NgbDropdownModule, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Language } from '../user/user-config.model';
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
export class HeaderComponent {
  isNavCollapsed = true;
  isValidUser = this.userService.isValidUser;
  hasRightsUser = this.userService.hasRightsUser;
  isAdminUser = this.userService.isAdminUser;
  userName = this.userService.userName;
  lang = this.userConfigService.lang;

  constructor(
    private userService: UserService,
    private userConfigService: UserConfigService,
    private router: Router,
    private modalService: NgbModal
  ) {}

  onSelectLang(lang: string) {
    // Enable language
    const language: Language = lang as Language;
    if (language) {
      const modalRef = this.modalService.open(ChangeLanguageComponent);
      modalRef.result.then(() => {
        this.userConfigService.setLang(language);
        this.closeSession();
      });
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
