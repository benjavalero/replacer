import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { PageTitle } from '../../../api/models/page-title';
import { PageApiService } from '../../../api/services/page-api.service';
import { UserConfigService } from '../../../core/services/user-config.service';
import { AlertComponent } from '../../../shared/alerts/alert-container/alert/alert.component';
import { AlertService } from '../../../shared/alerts/alert.service';
import { ReviewOptions } from '../review-options.model';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-custom-page-list',
  imports: [CommonModule, AlertComponent],
  templateUrl: './custom-page-list.component.html',
  styleUrls: []
})
export class CustomPageListComponent implements OnInit {
  options: ReviewOptions | null;
  pageTitles$!: Observable<PageTitle[]>;

  constructor(
    private route: ActivatedRoute,
    private alertService: AlertService,
    private pageApiService: PageApiService,
    private userConfigService: UserConfigService
  ) {
    this.options = null;
  }

  ngOnInit() {
    this.alertService.clearAlertMessages();

    const subtypeParam = this.route.snapshot.paramMap.get('subtype');
    const suggestionParam = this.route.snapshot.paramMap.get('suggestion');
    const csParam = this.route.snapshot.paramMap.get('cs');

    this.options = {
      kind: 1,
      subtype: subtypeParam!,
      suggestion: suggestionParam!,
      cs: csParam! === 'true'
    } as ReviewOptions;
    this.pageTitles$ = this.pageApiService.findPageTitlesToReviewByCustomType({ ...this.options });
  }

  get wikipediaUrl(): string {
    return `https://${this.userConfigService.lang()}.wikipedia.org/wiki/`;
  }
}
