import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '../alert/alert.service';
import { ReplacementListService } from '../replacement-list/replacement-list.service';
import { Language } from '../user/language-model';
import { UserService } from '../user/user.service';
import { PageReplacement } from './page-replacement.model';
import { PageDto, PageReview, PageSearch } from './page-review.model';
import { PageService } from './page.service';

@Component({
  selector: 'app-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: []
})
export class EditPageComponent implements OnInit {
  page: PageDto;
  replacements: PageReplacement[] = [];
  search: PageSearch;
  fixedCount = 0;

  constructor(
    private route: ActivatedRoute,
    private alertService: AlertService,
    private pageService: PageService,
    private router: Router,
    private userService: UserService,
    private titleService: Title,
    private replacementListService: ReplacementListService
  ) {}

  ngOnInit() {
    const pageId = +this.route.snapshot.paramMap.get('id');
    const filteredType = this.route.snapshot.paramMap.get('type');
    const filteredSubtype = this.route.snapshot.paramMap.get('subtype');
    const suggestion = this.route.snapshot.paramMap.get('suggestion');
    const caseSensitive = this.route.snapshot.paramMap.get('cs') === 'true';

    // First try to get the review from the cache
    const cachedReview = this.pageService.getPageReviewFromCache(pageId, filteredType, filteredSubtype);

    let htmlTitle = 'Replacer - ';
    if (filteredType && filteredSubtype) {
      htmlTitle += `${filteredSubtype} - `;
    }
    htmlTitle += cachedReview ? cachedReview.page.title : pageId;
    this.titleService.setTitle(htmlTitle);
    this.alertService.addInfoMessage('Buscando potenciales reemplazos del artículo…');

    if (cachedReview) {
      this.manageReview(cachedReview);
    } else {
      this.pageService.findPageReviewById(pageId, filteredType, filteredSubtype, suggestion, caseSensitive).subscribe(
        (review: PageReview) => {
          if (review) {
            this.manageReview(review);
          } else {
            // This alert will be short as it will be cleared on redirecting to next page
            this.alertService.addWarningMessage(
              'No se ha encontrado ningún reemplazo en la versión más actualizada del artículo'
            );
            this.redirectToNextPage({
              numPending: 0,
              type: filteredType,
              subtype: filteredSubtype,
              suggestion: suggestion,
              cs: caseSensitive
            });
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

    this.page = review.page;
    this.replacements = review.replacements;
    this.search = review.search;

    // Update count cache
    if (this.search.type && this.search.subtype) {
      this.replacementListService.updateSubtypeCount(this.search.type, this.search.subtype, this.search.numPending);
    }
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
      let contentToSave = this.page.content;
      fixedReplacements.forEach((rep) => {
        contentToSave = this.replaceText(contentToSave, rep.start, rep.text, rep.textFixed);
      });

      this.alertService.addInfoMessage(`Guardando cambios en «${this.page.title}»…`);
      this.saveContent(contentToSave);
    } else {
      // Save with no changes => Mark page as reviewed
      this.saveWithNoChanges();
    }
  }

  onSaveNoChanges() {
    // Save with no changes => Mark page as reviewed
    this.saveWithNoChanges();
  }

  private saveWithNoChanges() {
    this.alertService.addInfoMessage(`Marcando como revisado sin guardar cambios en «${this.page.title}»…`);
    this.saveContent(' ');
  }

  private saveContent(content: string) {
    // Remove replacements as a trick to hide the page
    this.replacements = [];

    // Decrement the count cache
    if (this.search.type && this.search.subtype) {
      this.replacementListService.decrementCount(this.search.type, this.search.subtype);
    }

    const savePage = { ...this.page, content: content };
    this.pageService.savePage(savePage, this.search).subscribe(
      (res) => {
        // Do nothing
      },
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
        // This alert will be short as it will be cleared on redirecting to next page
        this.alertService.addSuccessMessage('Cambios guardados con éxito');
        this.redirectToNextPage(this.search);
      }
    );
  }

  private redirectToNextPage(search: PageSearch) {
    if (search.type && search.subtype) {
      if (search.suggestion) {
        this.router.navigate([`random/${search.type}/${search.subtype}/${search.suggestion}/${search.cs}`]);
      } else {
        this.router.navigate([`random/${search.type}/${search.subtype}`]);
      }
    } else {
      this.router.navigate(['random']);
    }
  }

  private replaceText(fullText: string, position: number, currentText: string, newText: string): string {
    return fullText.slice(0, position) + newText + fullText.slice(position + currentText.length);
  }

  get url(): string {
    let url = `https://${this.page.lang || Language.es}.wikipedia.org/wiki/${this.page.title}`;
    if (this.page.section) {
      url += `#${this.page.section.title}`;
    }
    return url;
  }
}
