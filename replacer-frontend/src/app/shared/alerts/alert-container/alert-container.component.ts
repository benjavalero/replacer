import { AsyncPipe, NgForOf } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AlertService } from '../alert.service';
import { AlertComponent } from './alert/alert.component';

@Component({
  selector: 'app-alert-container',
  standalone: true,
  imports: [AsyncPipe, NgForOf, AlertComponent],
  templateUrl: './alert-container.component.html',
  styleUrls: ['./alert-container.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlertContainerComponent {
  alerts = this.alertService.alerts;

  constructor(private alertService: AlertService) {}
}
