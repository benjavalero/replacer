import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { AlertMessage } from './alert-message.model';
import { AlertComponent } from './alert.component';
import { AlertService } from './alert.service';

@Component({
  standalone: true,
  selector: 'app-alert-container',
  imports: [CommonModule, AlertComponent],
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
