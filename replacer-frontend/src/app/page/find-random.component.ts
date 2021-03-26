import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AlertService } from '../alert/alert.service';
import { PageReview } from './page-review.model';
import { PageService } from './page.service';
import { ValidateCustomComponent } from './validate-custom.component';
import { ValidateType } from './validate-custom.model';

@Component({
  selector: 'app-find-random',
  template: ``,
  styleUrls: []
})
export class FindRandomComponent implements OnInit {
  private filteredType: string;
  private filteredSubtype: string;
  private suggestion: string; // Only for type 'custom'
  private caseSensitive: boolean; // Only for type 'custom'

  constructor(
    private alertService: AlertService,
    private pageService: PageService,
    private router: Router,
    private route: ActivatedRoute,
    private titleService: Title,
    private modalService: NgbModal
  ) {}

  ngOnInit() {
    this.filteredType = this.route.snapshot.paramMap.get('type');
    this.filteredSubtype = this.route.snapshot.paramMap.get('subtype');
    this.suggestion = this.route.snapshot.paramMap.get('suggestion');
    this.caseSensitive = this.route.snapshot.paramMap.get('cs') === 'true';

    const msg =
      this.filteredType && this.filteredSubtype
        ? `Buscando artículo aleatorio de tipo «${this.filteredType} - ${this.filteredSubtype}»…`
        : 'Buscando artículo aleatorio…';
    this.titleService.setTitle(`Replacer - ${msg}`);
    this.alertService.addInfoMessage(msg);

    if (this.suggestion) {
      this.validateCustomReplacement();
    } else {
      this.findRandomPage();
    }
  }

  private findRandomPage(): void {
    this.pageService.findRandomPage(this.filteredType, this.filteredSubtype, this.suggestion, this.caseSensitive).subscribe(
      (review: PageReview) => {
        if (review) {
          // Cache the review
          this.pageService.putPageReviewInCache(this.filteredType, this.filteredSubtype, review);

          const pageId = review.page.id;
          // TODO : Do something with the title

          if (this.filteredType && this.filteredSubtype) {
            if (this.suggestion) {
              this.router.navigate([
                `article/${pageId}/${this.filteredType}/${this.filteredSubtype}/${this.suggestion}/${this.caseSensitive}`
              ]);
            } else {
              this.router.navigate([`article/${pageId}/${this.filteredType}/${this.filteredSubtype}`]);
            }
          } else {
            this.router.navigate([`article/${pageId}`]);
          }
        } else {
          this.alertService.addWarningMessage(
            this.filteredType && this.filteredSubtype
              ? `No se ha encontrado ningún artículo de tipo «${this.filteredType} - ${this.filteredSubtype}»`
              : 'No se ha encontrado ningún artículo'
          );
        }
      },
      (err) => {
        this.alertService.addErrorMessage('Error al buscar artículos con reemplazos: ' + err.error.message);
      }
    );
  }

  private validateCustomReplacement(): void {
    const replacement = this.filteredSubtype.trim();
    this.pageService
      .validateCustomReplacement(replacement, this.caseSensitive)
      .subscribe((validateType: ValidateType) => {
        if (validateType.type) {
          this.openValidationModal$(validateType.type, validateType.subtype).then((result) => {
            this.router.navigate([`random/${validateType.type}/${validateType.subtype}`]);
          });
        } else {
          this.findRandomPage();
        }
      });
  }

  private openValidationModal$(type: string, subtype: string): Promise<any> {
    const modalRef = this.modalService.open(ValidateCustomComponent);
    modalRef.componentInstance.type = type;
    modalRef.componentInstance.subtype = subtype;
    return modalRef.result;
  }
}
