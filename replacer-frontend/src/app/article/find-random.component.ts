import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AlertService } from '../alert/alert.service';
import { ArticleService } from './article.service';

@Component({
  selector: 'app-find-random',
  template: ``,
  styleUrls: []
})
export class FindRandomComponent implements OnInit {

  constructor(private alertService: AlertService, private articleService: ArticleService, private router: Router) { }

  ngOnInit() {
    this.alertService.addAlertMessage({
      type: 'primary',
      message: 'Buscando artículo aleatorio con reemplazos…'
    });

    this.articleService.findRandomArticle().subscribe((articleIds: number[]) => {
      const articleId = articleIds[0];
      if (articleId) {
        this.router.navigate([`article/${articleId}`]);
      } else {
        this.alertService.addAlertMessage({
          type: 'warning',
          message: 'No se ha encontrado ningún artículo con reemplazos'
        });
      }
    }, (err) => {
      this.alertService.addAlertMessage({
        type: 'danger',
        message: 'Error al buscar artículos con reemplazos: ' + err.error.message
      });
    });
  }

}
