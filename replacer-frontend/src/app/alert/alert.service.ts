import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AlertMessage } from './alert-message.model';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private readonly _alerts = new BehaviorSubject<AlertMessage[]>([]);
  readonly alerts$ = this._alerts.asObservable();

  constructor() {}

  private get alerts(): AlertMessage[] {
    return this._alerts.getValue();
  }

  private set alerts(alerts: AlertMessage[]) {
    this._alerts.next(alerts);
  }

  private addAlertMessage(alert: AlertMessage) {
    this.alerts = [...this.alerts, alert];
  }

  clearAlertMessages() {
    this.alerts = [];
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
