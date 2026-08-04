import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AlertComponent } from '../../../shared/alerts/alert-container/alert/alert.component';
import { UserLoginService } from '../../services/user-login.service';

@Component({
  selector: 'app-login',
  imports: [AlertComponent],
  templateUrl: './login.component.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {
  authorizationUrl = toSignal(inject(UserLoginService).getAuthorizationUrl$());
}
