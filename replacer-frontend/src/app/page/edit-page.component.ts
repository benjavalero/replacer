import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '../alert/alert.service';
import { AuthenticationService } from '../authentication/authentication.service';
import { Language } from '../authentication/wikipedia-user.model';
import { UserService } from '../user/user.service';
import { ArticleService } from './article.service';
import { PageReplacement } from './page-replacement.model';
import { PageReview } from './page-review.model';

@Component({
  selector: 'app-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: []
})
export class EditPageComponent implements OnInit {
  private articleId: number;
  filteredType: string;
  filteredSubtype: string;
  suggestion: string; // Only for type 'custom'

  private lang: Language;
  title: string;
  content: string;
  private section: number;
  private anchor: string;
  replacements: PageReplacement[] = [];
  numPending: number;
  fixedCount = 0;
  private currentTimestamp: string;

  constructor(
    private route: ActivatedRoute,
    private alertService: AlertService,
    private articleService: ArticleService,
    private router: Router,
    private authenticationService: AuthenticationService,
    private userService: UserService,
    private titleService: Title
  ) {}

  ngOnInit() {
    this.authenticationService.lang$.subscribe((lang: Language) => {
      this.lang = lang;
    });

    this.articleId = +this.route.snapshot.paramMap.get('id');
    this.filteredType = this.route.snapshot.paramMap.get('type');
    this.filteredSubtype = this.route.snapshot.paramMap.get('subtype');
    this.suggestion = this.route.snapshot.paramMap.get('suggestion');

    // First try to get the review from the cache
    const cachedReview = this.articleService.getPageReviewFromCache(
      this.articleId,
      this.filteredType,
      this.filteredSubtype
    );

    let htmlTitle = 'Replacer - ';
    if (this.filteredType && this.filteredSubtype) {
      htmlTitle += `${this.filteredSubtype} - `;
    }
    htmlTitle += cachedReview ? cachedReview.title : this.articleId;
    this.titleService.setTitle(htmlTitle);
    this.alertService.addInfoMessage('Buscando potenciales reemplazos del artículo…');

    if (cachedReview) {
      this.manageReview(cachedReview);
    } else {
      this.articleService
        .findPageReviewById(this.articleId, this.filteredType, this.filteredSubtype, this.suggestion)
        .subscribe(
          (review: PageReview) => {
            if (review) {
              this.manageReview(review);
            } else {
              this.alertService.addWarningMessage(
                'No se ha encontrado ningún reemplazo en la versión más actualizada del artículo'
              );
              this.redirectToNextArticle();
            }
          },
          (err) => {
            this.alertService.addErrorMessage('Error al buscar los reemplazos en el artículo: ' + err.error.message);
          }
        );
    }
  }

  private manageReview(review: PageReview) {
    this.alertService.clearAlertMessages();
    this.title = review.title;
    this.content = review.content;
    this.section = review.section;
    this.anchor = review.anchor;
    this.currentTimestamp = review.queryTimestamp;
    this.replacements = review.replacements;
    this.numPending = review.numPending;
  }

  onFixed(fixed: any) {
    this.replacements.find((rep) => rep.start === fixed.position).textFixed = fixed.newText;
    this.fixedCount = this.getFixedReplacements().length;
  }

  private getFixedReplacements(): PageReplacement[] {
    return this.replacements.filter((rep) => rep.textFixed);
  }

  onSaveChanges() {
    // Sort the fixed replacements in reverse to start by the end
    const fixedReplacements = this.getFixedReplacements().sort((a, b): number => b.start - a.start);
    if (fixedReplacements) {
      // Apply the fixes in the original text
      let contentToSave = this.content;
      fixedReplacements.forEach((rep) => {
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

    this.articleService
      .savePage(
        this.articleId,
        this.filteredType,
        this.filteredSubtype,
        this.title,
        content,
        this.section,
        this.currentTimestamp
      )
      .subscribe(
        (res) => {},
        (err) => {
          const errMsg = `Error al guardar el artículo: ${err.error}`;
          if (errMsg.includes('mwoauth-invalid-authorization')) {
            // Clear session and reload the page
            this.userService.clearSession();
            window.location.reload();
          } else {
            this.alertService.addErrorMessage(errMsg);
          }
        },
        () => {
          this.alertService.addSuccessMessage('Cambios guardados con éxito');
          this.redirectToNextArticle();
        }
      );
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
    return fullText.slice(0, position) + newText + fullText.slice(position + currentText.length);
  }

  get url(): string {
    let url = `https://${this.lang || Language.es}.wikipedia.org/wiki/${this.title}`;
    if (this.section && this.anchor) {
      url += `#${this.anchor}`;
    }
    return url;
  }
}
