import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AlertService } from '../alert/alert.service';

@Component({
  selector: 'app-find-custom',
  templateUrl: './find-custom.component.html',
  styleUrls: []
})
export class FindCustomComponent implements OnInit {

  replacement: string;
  suggestion: string;

  constructor(private router: Router, private alertService: AlertService) { }

  ngOnInit() {
    this.alertService.clearAlertMessages();
  }

  onSubmit() {
    this.router.navigate([`random/Personalizado/${this.replacement.trim()}/${this.suggestion.trim()}`]);
  }

}
