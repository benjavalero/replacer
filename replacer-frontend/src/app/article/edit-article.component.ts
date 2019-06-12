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
  content: string;
  replacements: ArticleReplacement[] = [];
  fixedCount = 0;

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
        this.content = review.content;
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

  onFixed(fixed: any) {
    this.replacements.find(rep => rep.start === fixed.position).textFixed = fixed.newText;
    this.fixedCount = this.getFixedReplacements().length;
  }

  private getFixedReplacements(): ArticleReplacement[] {
    return this.replacements.filter(rep => rep.textFixed);
  }

  onSaveChanges() {
    // Sort the fixed replacements in reverse to start by the end
    const fixedReplacements = this.getFixedReplacements().sort((a, b): number => b.start - a.start);
    if (fixedReplacements) {
      // Apply the fixes in the original text
      let contentToSave = this.content;
      fixedReplacements.forEach(rep => {
        contentToSave = this.replaceText(contentToSave, rep.start, rep.text, rep.textFixed);
      });
      this.saveContent(contentToSave);
    } else {
      // Save with no changes => Mark article as reviewed
      this.saveContent(' ');
    }
  }

  private saveContent(content: string) {
    // Remove replacements as a trick to hide the article
    this.replacements = [];

    this.alertService.addAlertMessage({
      type: 'info',
      message: `Guardando cambios en «${this.title}»…`
    });

    this.articleService.saveArticle(this.articleId, content).subscribe((res: boolean) => {
      this.alertService.addAlertMessage({
        type: 'success',
        message: 'Cambios guardados con éxito'
      });

      this.router.navigate(['random']);
    }, (err) => {
      this.alertService.addAlertMessage({
        type: 'danger',
        message: `Error al guardar el artículo: ${err.error.message}`
      });
    });
  }

  private replaceText(fullText: string, position: number, currentText: string, newText: string): string {
    return (
      fullText.slice(0, position) +
      newText +
      fullText.slice(position + currentText.length)
    );
  }

}
