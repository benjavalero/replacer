import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
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
  caseSensitive: boolean;

  constructor(private router: Router, private alertService: AlertService, private titleService: Title) {
    this.replacement = '';
    this.suggestion = '';
    this.caseSensitive = false;
  }

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazo personalizado');
    this.alertService.clearAlertMessages();
  }

  onSubmit() {
    const r = this.replacement.trim();
    const s = this.suggestion.trim();
    const cs = this.caseSensitive || false;

    this.alertService.clearAlertMessages();
    if (r === s) {
      this.alertService.addErrorMessage('El texto a reemplazar y el sugerido son iguales');
    } else {
      this.router.navigate([`random/Personalizado/${r}/${s}/${cs}`]);
    }
  }
}
