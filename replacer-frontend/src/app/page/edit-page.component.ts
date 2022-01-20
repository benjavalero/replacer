import { HttpStatusCode } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { AlertService } from '../alert/alert.service';
import { CUSTOM, REPLACEMENT_KINDS } from '../replacement-list/replacement-kind.model';
import { UserService } from '../user/user.service';
import { FixedReplacement, getReplacementEnd } from './page-replacement.model';
import { PageReviewResponse, ReviewOptions } from './page-review.model';
import { EMPTY_CONTENT, PageService } from './page.service';

@Component({
  selector: 'app-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: []
})
export class EditPageComponent implements OnChanges {
  readonly NUM_PENDING = 50;
  readonly CUSTOM_KIND = CUSTOM.code;

  @Input() review!: PageReviewResponse;

  private readonly THRESHOLD = 200; // Maximum number of characters to display around the replacements
  private fixedReplacements!: FixedReplacement[];

  @Output() saved: EventEmitter<ReviewOptions> = new EventEmitter();

  constructor(private alertService: AlertService, private pageService: PageService, private userService: UserService) {}

  ngOnChanges() {
    // We assume the replacements are returned sorted by the back-end
    this.fixedReplacements = new Array<FixedReplacement>(this.review.replacements.length);
  }

  // Calculate limits in order not to clash with other replacements
  limitLeft(index: number): number {
    const currentStart = this.review.replacements[index].start;
    let limit: number;
    if (index == 0) {
      limit = 0;
    } else {
      const previousEnd = getReplacementEnd(this.review.replacements[index - 1]);
      const diff = Math.floor((currentStart - previousEnd) / 2);
      limit = currentStart - diff;
    }
    return Math.max(limit, currentStart - this.THRESHOLD);
  }

  limitRight(index: number): number {
    const currentEnd = getReplacementEnd(this.review.replacements[index]);
    let limit: number;
    if (index == this.review.replacements.length - 1) {
      limit = this.review.page.content.length;
    } else {
      const nextStart = this.review.replacements[index + 1].start;
      const diff = Math.ceil((nextStart - currentEnd) / 2);
      limit = currentEnd + diff;
    }
    return Math.min(limit, currentEnd + this.THRESHOLD);
  }

  get fixedCount(): number {
    return this.filterFixedReplacements().length;
  }

  private filterFixedReplacements(): FixedReplacement[] {
    return this.fixedReplacements.filter((f) => !!f.newText);
  }

  onFixed(fixed: FixedReplacement) {
    this.fixedReplacements[fixed.index] = fixed;
  }

  onSaveChanges() {
    // Sort the fixed replacements in reverse to start by the end
    const fixedReplacements = this.filterFixedReplacements().sort((a, b): number => b.start - a.start);
    if (fixedReplacements) {
      // Apply the fixes in the original text
      let contentToSave = this.review.page.content;
      fixedReplacements.forEach((fix) => {
        contentToSave = this.replaceText(contentToSave, fix.start, fix.oldText, fix.newText!);
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
    this.saveContent(EMPTY_CONTENT);
  }

  private saveContent(content: string) {
    // Remove replacements as a trick to hide the page
    this.review.replacements = [];

    const savePage = { ...this.review.page, content: content };
    this.pageService.savePage(savePage, this.review.options).subscribe({
      error: (err) => {
        const errStatus = err.status;
        if (errStatus == HttpStatusCode.Conflict) {
          this.alertService.addErrorMessage(
            'Esta página de Wikipedia ha sido editada por otra persona. Recargue para revisarla de nuevo.'
          );
        } else if (errStatus == HttpStatusCode.Unauthorized) {
          // Clear session and reload the page
          this.userService.clearSession();
          window.location.reload();
        } else {
          this.alertService.addErrorMessage('Error al guardar la página');
        }
      },
      complete: () => {
        // This alert will be short as it will be cleared on redirecting to next page
        this.alertService.addSuccessMessage('Cambios guardados con éxito');

        this.saved.emit(
          new ReviewOptions(
            this.review.options.type || null,
            this.review.options.subtype || null,
            this.review.options.suggestion || null,
            this.review.options.cs || false
          )
        );
      }
    });
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

  get historyUrl(): string {
    return `https://${this.review.page.lang}.wikipedia.org/w/index.php?title=${this.review.page.title}&action=history`;
  }

  get typeLabel(): string {
    return REPLACEMENT_KINDS.get(this.review.options.type!)!.label;
  }
}
