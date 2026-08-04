import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { PageTitle } from '../../../api/models/page-title';
import { PageApiService } from '../../../api/services/page-api.service';
import { UserConfigService } from '../../../core/services/user-config.service';
import { AlertService } from '../../../shared/alerts/alert.service';
import { ReviewOptions } from '../review-options.model';
import { buildCustomReviewOptionsFromParamMap } from '../review-route-options.util';

@Component({
  selector: 'app-custom-page-list',
  imports: [CommonModule],
  templateUrl: './custom-page-list.component.html',
  styleUrls: []
})
export class CustomPageListComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly alertService = inject(AlertService);
  private readonly pageApiService = inject(PageApiService);
  private readonly userConfigService = inject(UserConfigService);

  options: ReviewOptions | null = null;
  pageTitles$!: Observable<PageTitle[]>;

  ngOnInit() {
    this.alertService.clearAlertMessages();

    this.options = buildCustomReviewOptionsFromParamMap(this.route.snapshot.paramMap);
    this.pageTitles$ = this.pageApiService.findPageTitlesToReviewByCustomType({ ...this.options });
  }

  get wikipediaUrl(): string {
    return `https://${this.userConfigService.lang()}.wikipedia.org/wiki/`;
  }
}
