import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthenticationService } from '../authentication/authentication.service';
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
  filteredType: string;
  filteredSubtype: string;
  suggestion: string; // Only for type 'custom'

  title = '';
  content: string;
  section: number;
  replacements: ArticleReplacement[] = [];
  fixedCount = 0;
  currentTimestamp: string;

  constructor(private route: ActivatedRoute, private alertService: AlertService, private articleService: ArticleService,
    private router: Router, private authenticationService: AuthenticationService) { }

  ngOnInit() {
    this.articleId = +this.route.snapshot.paramMap.get('id');
    this.filteredType = this.route.snapshot.paramMap.get('type');
    this.filteredSubtype = this.route.snapshot.paramMap.get('subtype');
    this.suggestion = this.route.snapshot.paramMap.get('suggestion');

    this.alertService.addInfoMessage('Buscando potenciales reemplazos del artículo…');

    // First try to get the review from the cache
    const cachedReview = this.articleService.getArticleReviewFromCache(this.articleId, this.filteredType, this.filteredSubtype);
    if (cachedReview) {
      this.manageReview(cachedReview);
    } else {
      this.articleService.findArticleReviewById(this.articleId, this.filteredType, this.filteredSubtype, this.suggestion)
        .subscribe((review: ArticleReview) => {
          if (review) {
            this.manageReview(review);
          } else {
            this.alertService.addWarningMessage('No se ha encontrado ningún reemplazo en la versión más actualizada del artículo');
            this.redirectToNextArticle();
          }
        }, (err) => {
          this.alertService.addErrorMessage('Error al buscar los reemplazos en el artículo: ' + err.error.message);
        });
    }
  }

  private manageReview(review: ArticleReview) {
    this.alertService.clearAlertMessages();
    this.title = review.title;
    this.content = review.content;
    this.section = review.section;
    this.currentTimestamp = review.currentTimestamp;
    this.replacements = review.replacements;
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

      this.alertService.addInfoMessage(`Guardando cambios en «${this.title}»…`);
      this.saveContent(contentToSave);
    } else {
      // Save with no changes => Mark article as reviewed
      this.saveWithNoChanges();
    }
  }

  onSaveNoChanges() {
    // Save with no changes => Mark article as reviewed
    this.saveWithNoChanges();
  }

  private saveWithNoChanges() {
    this.alertService.addInfoMessage(`Marcando como revisado sin guardar cambios en «${this.title}»…`);
    this.saveContent(' ');
  }

  private saveContent(content: string) {
    // Remove replacements as a trick to hide the article
    this.replacements = [];

    this.articleService.saveArticle(this.articleId, this.filteredType, this.filteredSubtype, content, this.section, this.currentTimestamp)
      .subscribe(res => { }, err => {
        const errMsg = `Error al guardar el artículo: ${err.error.message}`;
        if (errMsg.includes('mwoauth-invalid-authorization')) {
          // Clear session and reload the page
          this.authenticationService.clearSession();
          window.location.reload();
        } else {
          this.alertService.addErrorMessage(errMsg);
        }
      }, () => {
        this.alertService.addSuccessMessage('Cambios guardados con éxito');
        this.redirectToNextArticle();
      });
  }

  private redirectToNextArticle() {
    if (this.filteredType && this.filteredSubtype) {
      if (this.suggestion) {
        this.router.navigate([`random/${this.filteredType}/${this.filteredSubtype}/${this.suggestion}`]);
      } else {
        this.router.navigate([`random/${this.filteredType}/${this.filteredSubtype}`]);
      }
    } else {
      this.router.navigate(['random']);
    }
  }

  private replaceText(fullText: string, position: number, currentText: string, newText: string): string {
    return (
      fullText.slice(0, position) +
      newText +
      fullText.slice(position + currentText.length)
    );
  }

}
