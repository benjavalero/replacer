import { Component, OnInit } from '@angular/core';
import { AlertService } from '../alert/alert.service';

@Component({
  selector: 'app-find-replacement',
  templateUrl: './find-replacement.component.html',
  styleUrls: []
})
export class FindReplacementComponent implements OnInit {

  constructor(private alertService: AlertService) { }

  ngOnInit() {
    this.alertService.clearAlertMessages();
  }

}
