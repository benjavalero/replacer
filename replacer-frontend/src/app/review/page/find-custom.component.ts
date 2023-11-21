import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { AlertService } from '../../shared/alerts/alert.service';
import StringUtils from '../../shared/util/string-utils';

@Component({
  standalone: true,
  selector: 'app-find-custom',
  imports: [FormsModule, AlertComponent],
  templateUrl: './find-custom.component.html',
  styleUrls: []
})
export class FindCustomComponent implements OnInit {
  replacement: string;
  suggestion: string;
  caseSensitive: boolean;

  constructor(
    private router: Router,
    private alertService: AlertService,
    private titleService: Title
  ) {
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
      this.router.navigate([`review/custom/${r}/${s}/${cs}`]);
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
    } else if (!cs && StringUtils.compareStringAccent(replacement, suggestion) === 0) {
      this.alertService.addErrorMessage('El texto a reemplazar y el sugerido son iguales');
      return false;
    }
    return true;
  }
}
