import { Injectable, signal } from '@angular/core';
import { AlertMessage } from './alert-message.model';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  readonly alerts = signal<AlertMessage[]>([]);

  private addAlertMessage(alert: AlertMessage) {
    this.alerts.mutate((v) => v.push(alert));
  }

  clearAlertMessages() {
    this.alerts.set([]);
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
