import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { AlertService } from '../alert/alert.service';
import { ArticleService } from './article.service';

@Component({
  selector: 'app-find-random',
  template: ``,
  styleUrls: []
})
export class FindRandomComponent implements OnInit {

  private filteredType: string;
  private filteredSubtype: string;

  constructor(private alertService: AlertService, private articleService: ArticleService, private router: Router,
    private route: ActivatedRoute) { }

  ngOnInit() {
    this.filteredType = this.route.snapshot.paramMap.get('type');
    this.filteredSubtype = this.route.snapshot.paramMap.get('subtype');

    this.alertService.addInfoMessage((this.filteredType && this.filteredSubtype
      ? `Buscando artículo aleatorio de tipo «${this.filteredType} / ${this.filteredSubtype}»…`
      : 'Buscando artículo aleatorio con reemplazos…'));

    this.articleService.findRandomArticle(this.filteredType, this.filteredSubtype).subscribe((articleIds: number[]) => {
      const articleId = articleIds[0];
      if (articleId) {
        if (this.filteredType && this.filteredSubtype) {
          this.router.navigate([`article/${articleId}/${this.filteredType}/${this.filteredSubtype}`]);
        } else {
          this.router.navigate([`article/${articleId}`]);
        }
      } else {
        this.alertService.addWarningMessage((this.filteredType && this.filteredSubtype
          ? `No se ha encontrado ningún artículo de tipo «${this.filteredType} / ${this.filteredSubtype}»…`
          : 'No se ha encontrado ningún artículo con reemplazos…'));
      }
    }, (err) => {
      this.alertService.addErrorMessage('Error al buscar artículos con reemplazos: ' + err.error.message);
    });
  }

}
