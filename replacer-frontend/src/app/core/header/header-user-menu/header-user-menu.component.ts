import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbDropdown, NgbDropdownItem, NgbDropdownMenu, NgbDropdownToggle, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Language } from '../../model/language.model';
import { UserConfigService } from '../../services/user-config.service';
import { UserService } from '../../services/user.service';
import { ChangeLanguageComponent } from './change-language/change-language.component';

@Component({
  selector: 'app-header-user-menu',
  standalone: true,
  imports: [ChangeLanguageComponent, NgbDropdown, NgbDropdownToggle, NgbDropdownMenu, NgbDropdownItem],
  templateUrl: './header-user-menu.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderUserMenuComponent {
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
      modalRef.result.then(
        () => {
          this.userConfigService.setLang(language);
          this.closeSession();
        },
        () => {
          // Action cancelled. Nothing to do.
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
