import { Component, Input } from '@angular/core';
import { faExclamationCircle } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.css']
})
export class AlertComponent {
  @Input() type = 'primary';
  @Input() message: string;
  @Input() icon: boolean;

  warningIcon = faExclamationCircle;
}
