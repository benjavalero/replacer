import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Title } from '@angular/platform-browser';

import { AlertService } from '../alert/alert.service';
import { ArticleService } from './article.service';

@Component({
  selector: 'app-find-custom',
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
    private titleService: Title,
    private articleService: ArticleService
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Reemplazo personalizado');
    this.alertService.clearAlertMessages();
  }

  onSubmit() {
    let r = this.replacement.trim();
    let s = this.suggestion.trim();
    if (!this.caseSensitive) {
      r = r.toLocaleLowerCase('es');
      s = s.toLocaleLowerCase('es');
    }

    this.alertService.clearAlertMessages();
    if (r === s) {
      this.alertService.addErrorMessage('El texto a reemplazar y el sugerido son iguales');
    } else {
      this.articleService.validateCustomReplacement(r).subscribe((type: string) => {
        if (type) {
          this.alertService.addWarningMessage(`Reemplazo de tipo ${type} ya existente`);
          this.router.navigate([`random/${type}/${r}`]);
        } else {
          this.router.navigate([`random/Personalizado/${r}/${s}`]);
        }
      });
    }
  }
}
