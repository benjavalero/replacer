import { Injectable, Output, EventEmitter } from '@angular/core';
import { AlertMessage } from './alert-message.model';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  private alerts: AlertMessage[] = [];
  @Output() alertEvent: EventEmitter<AlertMessage[]> = new EventEmitter();

  constructor() { }

  addAlertMessage(alert: AlertMessage) {
    if (this.alerts.length === 5) {
      this.alerts.shift();
    }
    this.alerts.push(alert);
    this.alertEvent.emit(this.alerts);
  }

  clearAlertMessages() {
    this.alerts = [];
    this.alertEvent.emit(this.alerts);
  }

}
