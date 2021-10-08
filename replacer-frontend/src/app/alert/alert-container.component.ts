import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { AlertMessage } from './alert-message.model';
import { AlertService } from './alert.service';

@Component({
  selector: 'app-alert-container',
  templateUrl: './alert-container.component.html',
  styleUrls: ['./alert-container.component.css']
})
export class AlertContainerComponent implements OnInit {
  alerts$!: Observable<AlertMessage[]>;

  constructor(private alertService: AlertService) {}

  ngOnInit() {
    this.alerts$ = this.alertService.alerts$;
  }
}
