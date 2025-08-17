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
import { ReviewOptions } from './review-options.model';

export const kindLabel: { [key: number]: string } = {
  1: 'Personalizado',
  2: 'Ortografía',
  3: 'Compuestos',
  5: 'Estilo'
};

@Component({
  standalone: true,
  selector: 'app-find-random',
  imports: [CommonModule, EditPageComponent, AlertComponent],
  templateUrl: './find-random.component.html',
  styleUrls: []
})
export class FindRandomComponent implements OnInit {
  page: Page | null;
  options: ReviewOptions | null;
  numPending: number = 0;

  displayCustomWarning: boolean = false;

  constructor(
    private alertService: AlertService,
    private replacementTypeApiService: ReplacementTypeApiService,
    private pageApiService: PageApiService,
    private router: Router,
    private route: ActivatedRoute,
    private titleService: Title,
    private location: Location,
    private modalService: NgbModal,
    private userService: UserService
  ) {
    this.page = null;
    this.options = null;
  }

  ngOnInit() {
    // Optional search options
    const kindParam = this.route.snapshot.paramMap.get('kind');
    const subtypeParam = this.route.snapshot.paramMap.get('subtype');
    const suggestionParam = this.route.snapshot.paramMap.get('suggestion');
    const csParam = this.route.snapshot.paramMap.get('cs');

    // Build review-options object
    const options = {} as ReviewOptions;
    if (subtypeParam !== null) {
      options.subtype = subtypeParam;
      if (suggestionParam !== null) {
        // Custom type
        options.kind = 1;
        options.suggestion = suggestionParam;
        // If the suggestion is defined we can assume the cs is also defined
        options.cs = csParam! === 'true';
      } else {
        // If the subtype is defined we can assume the kind is also defined
        options.kind = +kindParam!;
      }
    }
    this.options = options;

    // If they type is custom, first of all we check if it is an existing standard one.
    if (this.isCustomType(options)) {
      this.validateExistingReplacement(options);
    }

    this.handleOptions(options);
  }

  private handleOptions(options: ReviewOptions): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam !== null) {
      this.findPageReview(+idParam, options);
    } else {
      this.findRandomPage(options);
    }
  }

  private findRandomPage(options: ReviewOptions): void {
    this.alertService.clearAlertMessages();
    this.checkCustomReplacementRights(options);

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
        if (page !== null) {
          this.manageReview(page, options);
        } else {
          this.alertService.addWarningMessage(
            options.kind && options.subtype
              ? `No se ha encontrado ningún artículo de tipo «${this.getKindLabel(options.kind)} - ${options.subtype}»`
              : 'No se ha encontrado ningún artículo'
          );
        }
      }
    });
  }

  private getKindLabel(kind: number): string {
    return kindLabel[kind];
  }

  private checkCustomReplacementRights(options: ReviewOptions): void {
    if (!this.canUseCustomReplacement(options)) {
      const path: string = `/review/list/${options.subtype}/${options.suggestion}/${options.cs}`;
      this.router.navigate([path]);
    }
  }

  private findPageReview(pageId: number, options: ReviewOptions): void {
    this.checkCustomReplacementRights(options);

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
          this.openExistingCustomModal$(replacementType.kind, replacementType.subtype).then(() => {
            this.router.navigate([this.getReviewUrl(replacementType as ReviewOptions, null)]);
          });
        } else {
          this.handleOptions(options);
        }
      });
  }

  private openExistingCustomModal$(kind: number, subtype: string): Promise<any> {
    const modalRef = this.modalService.open(ExistingCustomComponent);
    modalRef.componentInstance.kind = kindLabel[kind];
    modalRef.componentInstance.subtype = subtype;
    return modalRef.result;
  }

  private canUseCustomReplacement(options: ReviewOptions): boolean {
    const isSpecialUser: boolean = this.userService.isSpecialUser();
    const isBotUser: boolean = this.userService.isBotUser();
    return this.isCustomType(options) && (isSpecialUser || isBotUser);
  }
}
