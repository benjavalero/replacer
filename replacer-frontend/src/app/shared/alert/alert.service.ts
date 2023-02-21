import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AlertMessage } from './alert-message.model';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  readonly alerts$ = new BehaviorSubject<AlertMessage[]>([]);

  private get alerts(): AlertMessage[] {
    return this.alerts$.getValue();
  }

  private set alerts(alerts: AlertMessage[]) {
    this.alerts$.next(alerts);
  }

  private addAlertMessage(alert: AlertMessage) {
    this.alerts = [...this.alerts, alert];
  }

  clearAlertMessages() {
    this.alerts = [];
  }

  addInfoMessage(msg: string) {
    this.addMessage('primary', msg);
  }

  addSuccessMessage(msg: string) {
    this.addMessage('success', msg);
  }

  addWarningMessage(msg: string) {
    this.addMessage('warning', msg);
  }

  addErrorMessage(msg: string) {
    this.addMessage('danger', msg);
  }

  private addMessage(type: string, msg: string) {
    this.addAlertMessage(new AlertMessage(type, msg));
  }
}
