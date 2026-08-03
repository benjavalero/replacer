import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AlertService } from '../alert.service';
import { AlertComponent } from './alert/alert.component';

@Component({
  selector: 'app-alert-container',
  imports: [AlertComponent],
  templateUrl: './alert-container.component.html',
  styleUrls: ['./alert-container.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlertContainerComponent {
  private alertService = inject(AlertService);

  alerts = this.alertService.alerts;
}
