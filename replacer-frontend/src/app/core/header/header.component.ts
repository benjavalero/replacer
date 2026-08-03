import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import {
  NgbCollapse,
  NgbDropdown,
  NgbDropdownItem,
  NgbDropdownMenu,
  NgbDropdownToggle
} from '@ng-bootstrap/ng-bootstrap';
import { UserService } from '../services/user.service';
import { HeaderUserMenuComponent } from './header-user-menu/header-user-menu.component';

@Component({
  selector: 'app-header',
  imports: [
    NgClass,
    RouterLink,
    RouterLinkActive,
    NgbCollapse,
    NgbDropdown,
    NgbDropdownToggle,
    NgbDropdownMenu,
    NgbDropdownItem,
    HeaderUserMenuComponent
  ],
  templateUrl: './header.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderComponent {
  private userService = inject(UserService);

  isNavCollapsed = true;
  isValidUser = this.userService.isValidUser;
  hasRightsUser = this.userService.hasRightsUser;
  isAdminUser = this.userService.isAdminUser;
}
