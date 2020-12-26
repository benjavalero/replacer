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
  caseSensitive: boolean;

  constructor(private router: Router, private alertService: AlertService, private titleService: Title) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazo personalizado');
    this.alertService.clearAlertMessages();
  }

  onSubmit() {
    var r = this.replacement.trim();
    var s = this.suggestion.trim();
    if (!this.caseSensitive) {
      r = r.toLocaleLowerCase('es');
      s = s.toLocaleLowerCase('es');
    }

    this.alertService.clearAlertMessages();
    if (r == s) {
      this.alertService.addErrorMessage('El texto a reemplazar y el sugerido son iguales');
    } else {
      this.router.navigate([`random/Personalizado/${r}/${s}`]);
    }
  }
}
