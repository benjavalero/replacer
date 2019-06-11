import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AlertService } from '../alert/alert.service';
import { ArticleService } from './article.service';
import { ArticleReview } from './article-review.model';
import { ArticleReplacement } from './article-replacement.model';

@Component({
  selector: 'app-edit-article',
  templateUrl: './edit-article.component.html',
  styleUrls: []
})
export class EditArticleComponent implements OnInit {

  articleId: number;
  title = '';
  replacements: ArticleReplacement[] = [];

  constructor(private route: ActivatedRoute, private alertService: AlertService, private articleService: ArticleService,
    private router: Router) { }

  ngOnInit() {
    this.articleId = +this.route.snapshot.paramMap.get('id');
    this.alertService.addAlertMessage({
      type: 'primary',
      message: 'Buscando potenciales reemplazos del artículo…'
    });

    this.articleService.findArticleReviewById(this.articleId).subscribe((review: ArticleReview) => {
      if (review) {
        this.alertService.clearAlertMessages();
        this.title = review.title;
        this.replacements = review.replacements;
      } else {
        this.alertService.addAlertMessage({
          type: 'warning',
          message: 'No se ha encontrado ningún reemplazo en la versión más actualizada del artículo'
        });
        this.router.navigate(['random']);
      }
    }, (err) => {
      this.alertService.addAlertMessage({
        type: 'danger',
        message: 'Error al buscar los reemplazos en el artículo: ' + err.error.message
      });
    });
  }

}
