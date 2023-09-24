import { CommonModule } from '@angular/common';
import { HttpStatusCode } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faFastForward } from '@fortawesome/free-solid-svg-icons';
import { Observable } from 'rxjs';
import { Page } from '../../api/models/page';
import { Replacement } from '../../api/models/replacement';
import { ReviewedReplacement } from '../../api/models/reviewed-replacement';
import { ReviewedPage } from '../../api/models/reviewed-page';
import { PageApiService } from '../../api/services/page-api.service';
import { UserService } from '../../core/user/user.service';
import { AlertComponent } from '../../shared/alert/alert.component';
import { AlertService } from '../../shared/alert/alert.service';
import { sleep } from '../../shared/util/sleep';
import { EditSnippetComponent } from './edit-snippet.component';
import { kindLabel } from './find-random.component';
import { FixedReplacement, getReplacementEnd } from './page-replacement.model';
import { ReviewOptions } from './review-options.model';

@Component({
  standalone: true,
  selector: 'app-edit-page',
  imports: [CommonModule, AlertComponent, FontAwesomeModule, EditSnippetComponent],
  templateUrl: './edit-page.component.html',
  styleUrls: ['./edit-page.component.css']
})
export class EditPageComponent implements OnChanges {
  @Input() page!: Page;
  @Input() options!: ReviewOptions;
  @Input() numPending!: number;

  ffIcon = faFastForward;
  private readonly THRESHOLD = 200; // Maximum number of characters to display around the replacements
  private fixedReplacements!: FixedReplacement[];
  private reviewAllTypes: boolean = false;

  @Output() saved: EventEmitter<ReviewOptions> = new EventEmitter();

  private readonly lastSaveKey = 'lastSave';
  private readonly editionsPerMinute = 5;

  constructor(
    private alertService: AlertService,
    private userService: UserService,
    private pageApiService: PageApiService
  ) {}

  ngOnChanges() {
    // We assume the replacements are returned sorted by the back-end
    this.fixedReplacements = new Array<FixedReplacement>(this.page.replacements.length);

    // Reset filter by type
    this.reviewAllTypes = false;
  }

  displayReplacement(index: number): boolean {
    if (this.reviewAllTypes) {
      return true;
    } else {
      const options: ReviewOptions = this.options;
      if (options.kind && options.subtype) {
        const replacement = this.page.replacements[index];
        return replacement.kind === options.kind && replacement.subtype === options.subtype;
      } else {
        return true;
      }
    }
  }

  // Calculate limits in order not to clash with other replacements
  limitLeft(index: number): number {
    const currentStart = this.page.replacements[index].start;
    let limit: number;
    if (index == 0) {
      limit = 0;
    } else {
      const previousEnd = getReplacementEnd(this.page.replacements[index - 1]);
      const diff = Math.floor((currentStart - previousEnd) / 2);
      limit = currentStart - diff;
    }
    return Math.max(limit, currentStart - this.THRESHOLD);
  }

  limitRight(index: number): number {
    const currentEnd = getReplacementEnd(this.page.replacements[index]);
    let limit: number;
    if (index == this.page.replacements.length - 1) {
      limit = this.page.content.length;
    } else {
      const nextStart = this.page.replacements[index + 1].start;
      const diff = Math.ceil((nextStart - currentEnd) / 2);
      limit = currentEnd + diff;
    }
    return Math.min(limit, currentEnd + this.THRESHOLD);
  }

  get countOtherTypes(): number {
    if (this.reviewAllTypes) {
      return 0;
    } else {
      const options: ReviewOptions = this.options;
      if (options.kind && options.subtype) {
        let count: number = 0;
        for (let replacement of this.page.replacements) {
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
    if (fixedReplacements.length > 0) {
      // Apply the fixes in the original text
      let contentToSave = this.page.content;
      fixedReplacements.forEach((fix) => {
        contentToSave = this.replaceText(contentToSave, fix.start, fix.oldText, fix.newText!);
      });

      this.alertService.addInfoMessage(`Guardando cambios en «${this.page.title}»…`);
      this.saveContent(contentToSave);
    } else {
      // Save with no changes => Mark page as reviewed
      this.saveWithNoChanges();
    }
  }

  onSkip() {
    // Remove replacements as a trick to hide the page
    this.page.replacements = [];

    // Just move to the next page making the current one to move to the end of the queue
    this.nextPage();
  }

  onReviewAllTypes() {
    this.reviewAllTypes = true;

    // Trick to scroll up to the title
    document.querySelector('#pageTitle')!.scrollIntoView();
  }

  private saveWithNoChanges() {
    this.alertService.addInfoMessage(`Marcando como revisado sin guardar cambios en «${this.page.title}»…`);
    this.saveContent(null);
  }

  private saveContent(content: string | null) {
    const reviewedReplacements: ReviewedReplacement[] = this.getReviewedReplacements();

    // Remove replacements as a trick to hide the page
    this.page.replacements = [];

    // Build the reviewed page depending on changes or not
    let reviewedPage: ReviewedPage;
    if (content === null) {
      reviewedPage = {
        sectionOffset: this.page.section?.offset,
        reviewedReplacements: reviewedReplacements
      } as ReviewedPage;
    } else {
      reviewedPage = {
        title: this.page.title,
        content: content,
        sectionId: this.page.section?.id,
        sectionOffset: this.page.section?.offset,
        queryTimestamp: this.page.queryTimestamp,
        reviewedReplacements: reviewedReplacements
      } as ReviewedPage;
    }

    // Delay the saving in case there are other saving too recent
    let sleepTime = 0;
    const isBotUser: boolean = this.userService.isBotUser();
    if (!isBotUser && content !== null) {
      sleepTime = this.calculateSleepTime();
    }

    sleep(sleepTime).then(() => this.saveReview(reviewedPage));
  }

  private saveReview(reviewedPage: ReviewedPage): void {
    this.postSaveReview(reviewedPage).subscribe({
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
          this.alertService.addErrorMessage('Error al guardar la página: ' + (err.error?.message || err.message));
        }
      },
      complete: () => {
        // This alert will be short as it will be cleared on redirecting to next page
        this.alertService.addSuccessMessage('Cambios guardados con éxito');

        this.nextPage();
      }
    });
  }

  private postSaveReview(reviewedPage: ReviewedPage): Observable<void> {
    // Call backend and delay the observable response
    return this.pageApiService.saveReview({
      id: this.page.pageId,
      body: reviewedPage
    });
  }

  private calculateSleepTime(): number {
    // Store the date of the last save to check there are at least 12 s between savings (5 editions/min)
    // with an extra second of delay just in case
    let sleepTime = 0;
    const lastSave = localStorage.getItem(this.lastSaveKey);
    const now: number = Date.now();
    if (lastSave) {
      const minGap: number = (60 * 1000) / this.editionsPerMinute + 1000;
      const lastSaveDate: number = +lastSave;
      const gap: number = now - lastSaveDate;
      if (gap < minGap) {
        sleepTime = minGap - gap;
      }
    }

    // Store the new last save date
    const newSaveDate: number = now + sleepTime;
    localStorage.setItem(this.lastSaveKey, String(newSaveDate));

    return sleepTime;
  }

  private getReviewedReplacements(): ReviewedReplacement[] {
    const reviewedReplacements: ReviewedReplacement[] = [];
    for (let i = 0; i < this.page.replacements.length; i++) {
      const displayed: boolean = this.displayReplacement(i);
      if (displayed) {
        const replacement: Replacement = this.page.replacements[i];
        const fixed: boolean = this.fixedReplacements[i]?.isFixed() || false;
        // Destructure to delete unneeded properties
        const { suggestions, text, ...reviewed } = replacement;
        reviewedReplacements.push({ ...reviewed, fixed: fixed } as ReviewedReplacement);
      }
    }
    return reviewedReplacements;
  }

  private nextPage(): void {
    // This event will be called when the page is saved with or without changes, and also when skipped.
    this.saved.emit(this.options);
  }

  private replaceText(fullText: string, position: number, currentText: string, newText: string): string {
    return fullText.slice(0, position) + newText + fullText.slice(position + currentText.length);
  }

  get url(): string {
    let url = `https://${this.page.lang}.wikipedia.org/wiki/${this.page.title}`;
    if (this.page.section) {
      url += `#${this.page.section.title}`;
    }
    return url;
  }

  get historyUrl(): string {
    return `https://${this.page.lang}.wikipedia.org/w/index.php?title=${this.page.title}&action=history`;
  }

  get kindLabel(): string {
    return kindLabel[this.options.kind!];
  }
}
