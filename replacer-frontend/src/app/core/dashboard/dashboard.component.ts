import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { UserService } from '../services/user.service';
import { LoginComponent } from './login/login.component';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, AlertComponent, LoginComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent {
  private userService = inject(UserService);

  isValidUser = this.userService.isValidUser;
  hasRightsUser = this.userService.hasRightsUser;
  canUseCustomReplacement = this.userService.canUseCustomReplacement;

  constructor() {
    inject(Title).setTitle('Replacer - Reemplazador de la Wikipedia');
  }
}
