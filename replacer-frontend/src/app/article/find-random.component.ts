import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Title } from '@angular/platform-browser';

import { AlertService } from '../alert/alert.service';
import { ArticleService } from './article.service';
import { PageReview } from './page-review.model';

@Component({
  selector: 'app-find-random',
  template: ``,
  styleUrls: []
})
export class FindRandomComponent implements OnInit {

  private filteredType: string;
  private filteredSubtype: string;
  private suggestion: string; // Only for type 'custom'

  constructor(private alertService: AlertService, private articleService: ArticleService, private router: Router,
    private route: ActivatedRoute, private titleService: Title) { }

  ngOnInit() {
    this.filteredType = this.route.snapshot.paramMap.get('type');
    this.filteredSubtype = this.route.snapshot.paramMap.get('subtype');
    this.suggestion = this.route.snapshot.paramMap.get('suggestion');

    const msg = this.filteredType && this.filteredSubtype
      ? `Buscando artículo aleatorio de tipo «${this.filteredType} - ${this.filteredSubtype}»…`
      : 'Buscando artículo aleatorio…';
    this.titleService.setTitle(`Replacer - ${msg}`);
    this.alertService.addInfoMessage(msg);

    this.articleService.findRandomArticle(this.filteredType, this.filteredSubtype, this.suggestion).subscribe((review: PageReview) => {
      if (review) {
        // Cache the review
        this.articleService.putArticleReviewInCache(this.filteredType, this.filteredSubtype, review);

        const articleId = review.id;
        // TODO : Do something with the title

        if (this.filteredType && this.filteredSubtype) {
          if (this.suggestion) {
            this.router.navigate([`article/${articleId}/${this.filteredType}/${this.filteredSubtype}/${this.suggestion}`]);
          } else {
            this.router.navigate([`article/${articleId}/${this.filteredType}/${this.filteredSubtype}`]);
          }
        } else {
          this.router.navigate([`article/${articleId}`]);
        }
      } else {
        this.alertService.addWarningMessage((this.filteredType && this.filteredSubtype
          ? `No se ha encontrado ningún artículo de tipo «${this.filteredType} - ${this.filteredSubtype}»`
          : 'No se ha encontrado ningún artículo'));
      }
    }, (err) => {
      this.alertService.addErrorMessage('Error al buscar artículos con reemplazos: ' + err.error.message);
    });
  }

}
