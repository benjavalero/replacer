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

  private filteredWord: string;

  constructor(private alertService: AlertService, private articleService: ArticleService, private router: Router,
    private route: ActivatedRoute) { }

  ngOnInit() {
    this.filteredWord = this.route.snapshot.paramMap.get('word');

    this.alertService.addInfoMessage((this.filteredWord
      ? `Buscando artículo aleatorio con «${this.filteredWord}»…`
      : 'Buscando artículo aleatorio con reemplazos…'));

    this.articleService.findRandomArticle(this.filteredWord).subscribe((articleIds: number[]) => {
      const articleId = articleIds[0];
      if (articleId) {
        this.router.navigate([`article/${articleId}/${this.filteredWord || ''}`]);
      } else {
        this.alertService.addWarningMessage((this.filteredWord
          ? `No se ha encontrado ningún artículo con «${this.filteredWord}»…`
          : 'No se ha encontrado ningún artículo con reemplazos…'));
      }
    }, (err) => {
      this.alertService.addErrorMessage('Error al buscar artículos con reemplazos: ' + err.error.message);
    });
  }

}
