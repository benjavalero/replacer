import { CommonModule, Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Page } from '../../api/models/page';
import { ReplacementType } from '../../api/models/replacement-type';
import { PageApiService } from '../../api/services/page-api.service';
import { ReplacementTypeApiService } from '../../api/services/replacement-type-api.service';
import { StrictHttpResponse } from '../../api/strict-http-response';
import { UserService } from '../../core/services/user.service';
import { AlertComponent } from '../../shared/alerts/alert-container/alert/alert.component';
import { AlertService } from '../../shared/alerts/alert.service';
import { EditPageComponent } from './edit-page.component';
import { ExistingCustomComponent } from './existing-custom/existing-custom.component';
import { replacementKindLabels } from './replacement-kind-labels.const';
import { ReviewOptions } from './review-options.model';
import { buildReviewOptionsFromParamMap, getPageIdFromParamMap } from './review-route-options.util';

@Component({
  standalone: true,
  selector: 'app-find-random',
  imports: [CommonModule, EditPageComponent, AlertComponent],
  templateUrl: './find-random.component.html',
  styleUrls: []
})
export class FindRandomComponent implements OnInit {
  page: Page | null = null;
  options: ReviewOptions | null = null;
  numPending: number = 0;

  displayCustomWarning: boolean = false;

  constructor(
    private readonly alertService: AlertService,
    private readonly replacementTypeApiService: ReplacementTypeApiService,
    private readonly pageApiService: PageApiService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly titleService: Title,
    private readonly location: Location,
    private readonly modalService: NgbModal,
    private readonly userService: UserService
  ) {}

  ngOnInit() {
    const options = buildReviewOptionsFromParamMap(this.route.snapshot.paramMap);
    this.options = options;

    // For custom replacements, gate the search flow behind existing-type validation.
    // This avoids launching the review lookup twice during initialization.
    if (this.isCustomType(options)) {
      this.validateExistingReplacement(options);
      return;
    }

    this.handleOptions(options);
  }

  private handleOptions(options: ReviewOptions): void {
    const pageId = getPageIdFromParamMap(this.route.snapshot.paramMap);
    if (pageId === null) {
      this.findRandomPage(options);
      return;
    }

    this.findPageReview(pageId, options);
  }

  private findRandomPage(options: ReviewOptions): void {
    this.alertService.clearAlertMessages();
    if (!this.checkCustomReplacementRights(options)) {
      return;
    }

    const msg =
      options.kind && options.subtype
        ? `Buscando artículo aleatorio de tipo «${this.getKindLabel(options.kind)} - ${options.subtype}»…`
        : 'Buscando artículo aleatorio…';
    this.titleService.setTitle(`Replacer - ${msg}`);

    this.alertService.addInfoMessage(msg);

    this.pageApiService.findRandomPageWithReplacements$Response({ ...options }).subscribe({
      next: (response: StrictHttpResponse<Page>) => {
        const page: Page | null = response.body;
        this.numPending = Number(response.headers.get('X-Pagination-Total-Pages') ?? '0');
        if (this.isCustomType(options)) {
          this.displayCustomWarning = true;
        }
        if (page === null) {
          this.alertService.addWarningMessage(
            options.kind && options.subtype
              ? `No se ha encontrado ningún artículo de tipo «${this.getKindLabel(options.kind)} - ${options.subtype}»`
              : 'No se ha encontrado ningún artículo'
          );
          return;
        }

        this.manageReview(page, options);
      }
    });
  }

  private getKindLabel(kind: number): string {
    return replacementKindLabels[kind];
  }

  private checkCustomReplacementRights(options: ReviewOptions): boolean {
    if (this.isCustomType(options) && !this.canUseCustomReplacement()) {
      const path: string = `/review/list/${options.subtype}/${options.suggestion}/${options.cs}`;
      this.router.navigate([path]);
      return false;
    }

    return true;
  }

  private findPageReview(pageId: number, options: ReviewOptions): void {
    // Stop immediately after redirecting unauthorized users to the listing view.
    if (!this.checkCustomReplacementRights(options)) {
      return;
    }

    this.pageApiService.findPageReviewById$Response({ ...options, id: pageId }).subscribe({
      next: (response: StrictHttpResponse<Page>) => {
        const page: Page | null = response.body;
        this.numPending = Number(response.headers.get('X-Pagination-Total-Pages') ?? '0');
        if (this.isCustomType(options)) {
          this.alertService.clearAlertMessages();
          this.displayCustomWarning = true;
        }
        if (page) {
          this.manageReview(page, options);
        } else {
          // This alert will be short as it will be cleared on redirecting to next page
          this.alertService.addWarningMessage(
            'No se ha encontrado ningún reemplazo en la versión más actualizada del artículo'
          );
          this.findRandomPage(options);
        }
      }
    });
  }

  private manageReview(page: Page, options: ReviewOptions): void {
    this.alertService.clearAlertMessages();

    this.page = page;

    // Modify title
    let htmlTitle = 'Replacer - ';
    if (options.kind && options.subtype) {
      htmlTitle += `${options.subtype} - `;
    }
    htmlTitle += page.title;
    this.titleService.setTitle(htmlTitle);

    this.setReviewUrl(options, page.pageId);
  }

  private setReviewUrl(options: ReviewOptions, pageId: number | null): void {
    this.location.replaceState(this.getReviewUrl(options, pageId));
  }

  private getReviewUrl(options: ReviewOptions, pageId: number | null): string {
    let path: string;
    if (options.kind && options.subtype) {
      if (options.suggestion) {
        path = `/review/custom/${options.subtype}/${options.suggestion}/${options.cs}`;
      } else {
        path = `/review/${options.kind}/${options.subtype}`;
      }
    } else {
      path = '/review/notype';
    }

    if (pageId) {
      path = `${path}/${pageId}`;
    }

    return path;
  }

  onSaved(options: ReviewOptions) {
    this.setReviewUrl(options, null);
    this.findRandomPage(options);
  }

  private isCustomType(options: ReviewOptions): boolean {
    return options.kind === 1;
  }

  private validateExistingReplacement(options: ReviewOptions): void {
    this.replacementTypeApiService
      .findReplacementType({
        replacement: options.subtype!.trim(),
        cs: options.cs!
      })
      .subscribe((replacementType: ReplacementType) => {
        if (replacementType) {
          const existingKindLabel = replacementKindLabels[replacementType.kind];
          this.openExistingCustomModal$(existingKindLabel, replacementType.subtype).then(() => {
            this.router.navigate([this.getReviewUrl(replacementType as ReviewOptions, null)]);
          });
        } else {
          this.handleOptions(options);
        }
      });
  }

  private openExistingCustomModal$(kindLabel: string, subtype: string): Promise<void> {
    const modalRef = this.modalService.open(ExistingCustomComponent);
    modalRef.componentInstance.kindLabel = kindLabel;
    modalRef.componentInstance.subtype = subtype;
    return modalRef.result as Promise<void>;
  }

  private canUseCustomReplacement(): boolean {
    const isSpecialUser: boolean = this.userService.isSpecialUser();
    const isBotUser: boolean = this.userService.isBotUser();
    return isSpecialUser || isBotUser;
  }
}
