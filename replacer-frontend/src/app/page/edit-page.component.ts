import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { AlertService } from '../alert/alert.service';
import { ReplacementListService } from '../replacement-list/replacement-list.service';
import { UserService } from '../user/user.service';
import { PageReplacement } from './page-replacement.model';
import { PageReview, ReviewOptions } from './page-review.model';
import { PageService } from './page.service';

@Component({
  selector: 'app-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: []
})
export class EditPageComponent implements OnChanges {
  @Input() review: PageReview;

  fixedCount = 0;

  @Output() saved: EventEmitter<ReviewOptions> = new EventEmitter();

  constructor(
    private alertService: AlertService,
    private pageService: PageService,
    private userService: UserService,
    private replacementListService: ReplacementListService
  ) {}

  ngOnChanges() {
    this.fixedCount = 0;
  }

  onFixed(fixed: any) {
    this.review.replacements.find((rep) => rep.start === fixed.position).textFixed = fixed.newText;
    this.fixedCount = this.getFixedReplacements().length;
  }

  private getFixedReplacements(): PageReplacement[] {
    return this.review.replacements.filter((rep) => rep.textFixed);
  }

  onSaveChanges() {
    // Sort the fixed replacements in reverse to start by the end
    const fixedReplacements = this.getFixedReplacements().sort((a, b): number => b.start - a.start);
    if (fixedReplacements) {
      // Apply the fixes in the original text
      let contentToSave = this.review.page.content;
      fixedReplacements.forEach((rep) => {
        contentToSave = this.replaceText(contentToSave, rep.start, rep.text, rep.textFixed);
      });

      this.alertService.addInfoMessage(`Guardando cambios en «${this.review.page.title}»…`);
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
    this.alertService.addInfoMessage(`Marcando como revisado sin guardar cambios en «${this.review.page.title}»…`);
    this.saveContent(' ');
  }

  private saveContent(content: string) {
    // Remove replacements as a trick to hide the page
    this.review.replacements = [];

    // Decrement the count cache
    if (this.review.search.type && this.review.search.subtype) {
      this.replacementListService.decrementCount(this.review.search.type, this.review.search.subtype);
    }

    const savePage = { ...this.review.page, content: content };
    this.pageService.savePage(savePage, this.review.search).subscribe(
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

        this.saved.emit({
          type: this.review.search.type,
          subtype: this.review.search.subtype,
          suggestion: this.review.search.suggestion,
          cs: this.review.search.cs
        });
      }
    );
  }

  private replaceText(fullText: string, position: number, currentText: string, newText: string): string {
    return fullText.slice(0, position) + newText + fullText.slice(position + currentText.length);
  }

  get url(): string {
    let url = `https://${this.review.page.lang}.wikipedia.org/wiki/${this.review.page.title}`;
    if (this.review.page.section) {
      url += `#${this.review.page.section.title}`;
    }
    return url;
  }
}
