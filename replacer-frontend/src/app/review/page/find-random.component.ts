import { CommonModule, Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ReplacementType } from '../../api/models/replacement-type';
import { ReviewPage } from '../../api/models/review-page';
import { ReplacementTypeApiService } from '../../api/services/replacement-type-api.service';
import { PageApiService } from '../../api/services/page-api.service';
import { AlertService } from '../../shared/alert/alert.service';
import { EditPageComponent } from './edit-page.component';
import { ReviewOptions } from './review-options.model';
import { ValidateCustomComponent } from './validate-custom.component';
import { StrictHttpResponse } from '../../api/strict-http-response';

export const kindLabel: { [key: number]: string } = {
  1: 'Personalizado',
  2: 'Ortografía',
  3: 'Compuestos',
  5: 'Estilo'
};

@Component({
  standalone: true,
  selector: 'app-find-random',
  imports: [CommonModule, EditPageComponent, ValidateCustomComponent],
  template: `
    <app-edit-page
      *ngIf="review && options"
      [review]="review"
      [options]="options"
      [numPending]="numPending"
      (saved)="onSaved($event)"
    ></app-edit-page>
  `,
  styleUrls: []
})
export class FindRandomComponent implements OnInit {
  review: ReviewPage | null;
  options: ReviewOptions | null;
  numPending: number = 0;

  constructor(
    private alertService: AlertService,
    private replacementTypeApiService: ReplacementTypeApiService,
    private pageApiService: PageApiService,
    private router: Router,
    private route: ActivatedRoute,
    private titleService: Title,
    private location: Location,
    private modalService: NgbModal
  ) {
    this.review = null;
    this.options = null;
  }

  ngOnInit() {
    // Optional search options
    const idParam = this.route.snapshot.paramMap.get('id');
    const kindParam = this.route.snapshot.paramMap.get('kind');
    const subtypeParam = this.route.snapshot.paramMap.get('subtype');
    const suggestionParam = this.route.snapshot.paramMap.get('suggestion');
    const csParam = this.route.snapshot.paramMap.get('cs');
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

    if (idParam !== null) {
      this.findPageReview(+idParam, options);
    } else if (options.suggestion !== undefined) {
      this.validateCustomReplacement(options);
    } else {
      this.findRandomPage(options);
    }
  }

  private findRandomPage(options: ReviewOptions): void {
    const msg =
      options.kind && options.subtype
        ? `Buscando artículo aleatorio de tipo «${this.getKindLabel(options.kind)} - ${options.subtype}»…`
        : 'Buscando artículo aleatorio…';
    this.titleService.setTitle(`Replacer - ${msg}`);

    this.alertService.addInfoMessage(msg);

    this.pageApiService.findRandomPageWithReplacements$Response({ ...options }).subscribe({
      next: (response: StrictHttpResponse<ReviewPage>) => {
        const review: ReviewPage | null = response.body;
        const numPending: number = Number(response.headers.get('X-Pagination-Total-Pages') ?? '0');
        if (review !== null) {
          this.manageReview(review, options, numPending);
        } else {
          this.alertService.addWarningMessage(
            options.kind && options.subtype
              ? `No se ha encontrado ningún artículo de tipo «${this.getKindLabel(options.kind)} - ${options.subtype}»`
              : 'No se ha encontrado ningún artículo'
          );
        }
      },
      error: (err) => {
        this.alertService.addErrorMessage(
          'Error al buscar artículos con reemplazos: ' + (err.error?.message || err.message)
        );
      }
    });
  }

  private getKindLabel(kind: number): string {
    return kindLabel[kind];
  }

  private findPageReview(pageId: number, options: ReviewOptions): void {
    this.pageApiService.findPageReviewById$Response({ ...options, id: pageId }).subscribe({
      next: (response: StrictHttpResponse<ReviewPage>) => {
        const review: ReviewPage | null = response.body;
        const numPending: number = Number(response.headers.get('X-Pagination-Total-Pages') ?? '0');
        if (review) {
          this.manageReview(review, options, numPending);
        } else {
          // This alert will be short as it will be cleared on redirecting to next page
          this.alertService.addWarningMessage(
            'No se ha encontrado ningún reemplazo en la versión más actualizada del artículo'
          );
          this.findRandomPage(options);
        }
      },
      error: (err) => {
        this.alertService.addErrorMessage(
          'Error al buscar los reemplazos en el artículo: ' + (err.error?.message || err.message)
        );
      }
    });
  }

  private manageReview(review: ReviewPage, options: ReviewOptions, numPending: number): void {
    this.alertService.clearAlertMessages();

    this.options = options;
    this.review = review;
    this.numPending = numPending;

    // Modify title
    let htmlTitle = 'Replacer - ';
    if (options.kind && options.subtype) {
      htmlTitle += `${options.subtype} - `;
    }
    htmlTitle += review.title;
    this.titleService.setTitle(htmlTitle);

    this.setReviewUrl(options, review.pageId);
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
      path = '/review';
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

  private validateCustomReplacement(options: ReviewOptions): void {
    const replacement = options.subtype!.trim();
    this.replacementTypeApiService
      .validateCustomReplacement({
        replacement: replacement,
        cs: options.cs!
      })
      .subscribe((validateType: ReplacementType) => {
        if (validateType) {
          this.openValidationModal$(validateType.kind, validateType.subtype).then((result) => {
            this.router.navigate([this.getReviewUrl(validateType as ReviewOptions, null)]);
          });
        } else {
          this.findRandomPage(options);
        }
      });
  }

  private openValidationModal$(kind: number, subtype: string): Promise<any> {
    const modalRef = this.modalService.open(ValidateCustomComponent);
    modalRef.componentInstance.kind = kindLabel[kind];
    modalRef.componentInstance.subtype = subtype;
    return modalRef.result;
  }
}
