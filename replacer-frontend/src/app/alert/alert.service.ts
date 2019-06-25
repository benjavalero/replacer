import { Injectable, Output, EventEmitter } from '@angular/core';
import { AlertMessage } from './alert-message.model';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  private alerts: AlertMessage[] = [];
  @Output() alertEvent: EventEmitter<AlertMessage[]> = new EventEmitter();

  constructor() { }

  private addAlertMessage(alert: AlertMessage) {
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

  addInfoMessage(msg: string) {
    this.addAlertMessage({
      type: 'primary',
      message: msg
    });
  }

  addSuccessMessage(msg: string) {
    this.addAlertMessage({
      type: 'success',
      message: msg
    });
  }

  addWarningMessage(msg: string) {
    this.addAlertMessage({
      type: 'warning',
      message: msg
    });
  }

  addErrorMessage(msg: string) {
    this.addAlertMessage({
      type: 'danger',
      message: msg
    });
  }

}
