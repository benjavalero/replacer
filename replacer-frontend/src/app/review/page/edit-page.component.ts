import { HttpStatusCode } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { faFastForward } from '@fortawesome/free-solid-svg-icons';
import { UserService } from '../../core/user/user.service';
import { AlertService } from '../../shared/alert/alert.service';
import { FixedReplacement, getReplacementEnd, ReviewReplacement } from './page-replacement.model';
import {
  kindLabel,
  PageReviewOptions,
  PageReviewResponse,
  ReviewedReplacement,
  ReviewOptions
} from './page-review.model';
import { EMPTY_CONTENT, PageService } from './page.service';

@Component({
  selector: 'app-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: ['./edit-page.component.css']
})
export class EditPageComponent implements OnChanges {
  @Input() review!: PageReviewResponse;

  ffIcon = faFastForward;
  private readonly THRESHOLD = 200; // Maximum number of characters to display around the replacements
  private fixedReplacements!: FixedReplacement[];
  private reviewAllTypes: boolean = false;

  @Output() saved: EventEmitter<ReviewOptions> = new EventEmitter();

  constructor(private alertService: AlertService, private pageService: PageService, private userService: UserService) {}

  ngOnChanges() {
    // We assume the replacements are returned sorted by the back-end
    this.fixedReplacements = new Array<FixedReplacement>(this.review.replacements.length);

    // Reset filter by type
    this.reviewAllTypes = false;
  }

  displayReplacement(index: number): boolean {
    if (this.reviewAllTypes) {
      return true;
    } else {
      const options: PageReviewOptions = this.review.options;
      if (options.kind && options.subtype) {
        const replacement = this.review.replacements[index];
        return replacement.kind === options.kind && replacement.subtype === options.subtype;
      } else {
        return true;
      }
    }
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

  get countOtherTypes(): number {
    if (this.reviewAllTypes) {
      return 0;
    } else {
      const options: PageReviewOptions = this.review.options;
      if (options.kind && options.subtype) {
        let count: number = 0;
        for (let replacement of this.review.replacements) {
          if (replacement.kind !== options.kind || replacement.subtype !== options.subtype) {
            count++;
          }
        }
        return count;
      } else {
        return 0;
      }
    }
  }

  get fixedCount(): number {
    return this.filterFixedReplacements().length;
  }

  private filterFixedReplacements(): FixedReplacement[] {
    return this.fixedReplacements.filter((f) => f.isFixed());
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

  onSkip() {
    // Remove replacements as a trick to hide the page
    this.review.replacements = [];

    // Just move to the next page making the current one to move to the end of the queue
    this.nextPage();
  }

  onReviewAllTypes() {
    this.reviewAllTypes = true;

    // Trick to scroll up to the title
    document.querySelector('#pageTitle')!.scrollIntoView();
  }

  private saveWithNoChanges() {
    this.alertService.addInfoMessage(`Marcando como revisado sin guardar cambios en «${this.review.page.title}»…`);
    this.saveContent(EMPTY_CONTENT);
  }

  private saveContent(content: string) {
    const reviewedReplacements = this.getReviewedReplacements();

    // Remove replacements as a trick to hide the page
    this.review.replacements = [];

    const savePage = { ...this.review.page, content: content };
    this.pageService.saveReview(savePage, reviewedReplacements).subscribe({
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

        this.nextPage();
      }
    });
  }

  private getReviewedReplacements(): ReviewedReplacement[] {
    const reviewedReplacements: ReviewedReplacement[] = [];
    for (let i = 0; i < this.review.replacements.length; i++) {
      const displayed: boolean = this.displayReplacement(i);
      if (displayed) {
        const replacement: ReviewReplacement = this.review.replacements[i];
        const fixed: boolean = this.fixedReplacements[i]?.isFixed() || false;
        const reviewed = new ReviewedReplacement(
          replacement.kind,
          replacement.subtype,
          replacement.cs,
          replacement.start,
          fixed
        );
        reviewedReplacements.push(reviewed);
      }
    }
    return reviewedReplacements;
  }

  private nextPage(): void {
    // This event will be called when the page is saved with or without changes, and also when skipped.
    this.saved.emit(
      new ReviewOptions(
        this.review.options.kind || null,
        this.review.options.subtype || null,
        this.review.options.suggestion || null,
        this.review.options.cs || false
      )
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

  get historyUrl(): string {
    return `https://${this.review.page.lang}.wikipedia.org/w/index.php?title=${this.review.page.title}&action=history`;
  }

  get kindLabel(): string {
    return kindLabel[this.review.options.kind!];
  }
}
