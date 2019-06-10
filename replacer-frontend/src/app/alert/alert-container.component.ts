import { Component, OnInit } from '@angular/core';

import { AlertMessage } from './alert-message.model';
import { AlertService } from './alert.service';

@Component({
  selector: 'app-alert-container',
  templateUrl: './alert-container.component.html',
  styleUrls: []
})
export class AlertContainerComponent implements OnInit {

  alerts: AlertMessage[];

  constructor(private alertService: AlertService) { }

  ngOnInit() {
    this.alerts = [];
    this.alertService.alertEvent.subscribe((alerts: AlertMessage[]) => this.alerts = alerts);
  }

}
