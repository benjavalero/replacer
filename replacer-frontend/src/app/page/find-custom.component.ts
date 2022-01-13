import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { AlertService } from '../alert/alert.service';
import StringUtils from '../string-utils';

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
  }

  onSubmit() {
    const r = this.replacement.trim();
    const s = this.suggestion.trim();
    const cs = this.caseSensitive || false;

    if (this.validate(r, s, cs)) {
      this.router.navigate([`custom/${r}/${s}/${cs}`]);
    }
  }

  private validate(replacement: string, suggestion: string, cs: boolean): boolean {
    this.alertService.clearAlertMessages();

    if (!replacement || !suggestion) {
      this.alertService.addErrorMessage('El reemplazo y la sugerencia son obligatorios');
      return false;
    } else if (replacement === suggestion) {
      this.alertService.addErrorMessage('El texto a reemplazar y el sugerido son iguales');
      return false;
    } else if (!cs && StringUtils.compareString(replacement, suggestion) === 0) {
      this.alertService.addErrorMessage('El texto a reemplazar y el sugerido son iguales');
      return false;
    }
    return true;
  }
}
