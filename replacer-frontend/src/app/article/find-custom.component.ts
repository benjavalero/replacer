import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Title } from '@angular/platform-browser';

import { AlertService } from '../alert/alert.service';

@Component({
  selector: 'app-find-custom',
  templateUrl: './find-custom.component.html',
  styleUrls: []
})
export class FindCustomComponent implements OnInit {

  replacement: string;
  suggestion: string;

  constructor(private router: Router, private alertService: AlertService, private titleService: Title) { }

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazo personalizado');
    this.alertService.clearAlertMessages();
  }

  onSubmit() {
    this.router.navigate([`random/Personalizado/${this.replacement.trim()}/${this.suggestion.trim()}`]);
  }

}
