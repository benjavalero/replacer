import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AlertService } from '../../shared/alert/alert.service';
import { kindLabel, PageReviewResponse, ReviewOptions } from './page-review.model';
import { PageService } from './page.service';
import { ValidateCustomComponent } from './validate-custom.component';
import { ReplacementValidationResponse } from './validate-custom.model';

@Component({
  selector: 'app-find-random',
  template: `
    <app-edit-page *ngIf="review" [review]="review" (saved)="onSaved($event)"></app-edit-page>
  `,
  styleUrls: []
})
export class FindRandomComponent implements OnInit {
  review: PageReviewResponse | null;

  constructor(
    private alertService: AlertService,
    private pageService: PageService,
    private router: Router,
    private route: ActivatedRoute,
    private titleService: Title,
    private location: Location,
    private modalService: NgbModal
  ) {
    this.review = null;
  }

  ngOnInit() {
    // Optional search options
    const pageId = this.route.snapshot.paramMap.get('id');
    const options = new ReviewOptions(
      this.convertKindParameter(this.route.snapshot.paramMap.get('kind')),
      this.route.snapshot.paramMap.get('subtype'),
      this.route.snapshot.paramMap.get('suggestion'),
      this.route.snapshot.paramMap.get('cs') === 'true'
    );
    if (options.suggestion) {
      options.kind = 1;
    }

    if (pageId) {
      this.findPageReview(+pageId, options);
    } else if (options.suggestion) {
      this.validateCustomReplacement(options);
    } else {
      this.findRandomPage(options);
    }
  }

  private convertKindParameter(kind: string | null): number | null {
    if (kind) {
      // Just for compatibility with old use with labels
      switch (kind) {
        case 'Personalizado': {
          return 1;
        }
        case 'Ortografía': {
          return 2;
        }
        case 'Compuestos': {
          return 3;
        }
        default: {
          return +kind;
        }
      }
    } else {
      return null;
    }
  }

  private findRandomPage(options: ReviewOptions): void {
    const msg =
      options.kind && options.subtype
        ? `Buscando artículo aleatorio de tipo «${options.getKindLabel()} - ${options.subtype}»…`
        : 'Buscando artículo aleatorio…';
    this.titleService.setTitle(`Replacer - ${msg}`);

    this.alertService.addInfoMessage(msg);

    this.pageService.findRandomPage(options).subscribe({
      next: (review: PageReviewResponse) => {
        if (review) {
          this.manageReview(review, options);
        } else {
          this.alertService.addWarningMessage(
            options.kind && options.subtype
              ? `No se ha encontrado ningún artículo de tipo «${options.getKindLabel()} - ${options.subtype}»`
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

  private findPageReview(pageId: number, options: ReviewOptions): void {
    this.pageService.findPageReviewById(pageId, options).subscribe({
      next: (review: PageReviewResponse) => {
        if (review) {
          this.manageReview(review, options);
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

  private manageReview(review: PageReviewResponse, options: ReviewOptions): void {
    this.alertService.clearAlertMessages();

    this.review = review;

    // Modify title
    let htmlTitle = 'Replacer - ';
    if (options.kind && options.subtype) {
      htmlTitle += `${options.subtype} - `;
    }
    htmlTitle += review.page.title;
    this.titleService.setTitle(htmlTitle);

    this.setReviewUrl(options, review.page.id);
  }

  private setReviewUrl(options: ReviewOptions, pageId: number | null): void {
    this.location.replaceState(this.getReviewUrl(options, pageId));
  }

  private getReviewUrl(options: ReviewOptions, pageId: number | null): string {
    let path: string;
    if (options.kind && options.subtype) {
      if (options.suggestion) {
        path = `/custom/${options.subtype}/${options.suggestion}/${options.cs}`;
      } else {
        path = `/list/${options.kind}/${options.subtype}`;
      }
    } else {
      path = '/random';
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
    this.pageService
      .validateCustomReplacement(replacement, options.cs!)
      .subscribe((validateType: ReplacementValidationResponse) => {
        if (validateType) {
          this.openValidationModal$(validateType.kind, validateType.subtype).then((result) => {
            const knownTypeOptions = new ReviewOptions(validateType.kind, validateType.subtype, null, null);
            this.router.navigate([this.getReviewUrl(knownTypeOptions, null)]);
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
