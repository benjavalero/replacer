import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { PageTitle } from '../../../api/models/page-title';
import { PageApiService } from '../../../api/services/page-api.service';
import { UserConfigService } from '../../../core/services/user-config.service';
import { AlertComponent } from '../../../shared/alerts/alert-container/alert/alert.component';
import { AlertService } from '../../../shared/alerts/alert.service';
import { ReviewOptions } from '../review-options.model';
import { buildCustomReviewOptionsFromParamMap } from '../review-route-options.util';

@Component({
  standalone: true,
  selector: 'app-custom-page-list',
  imports: [CommonModule, AlertComponent],
  templateUrl: './custom-page-list.component.html',
  styleUrls: []
})
export class CustomPageListComponent implements OnInit {
  options: ReviewOptions | null = null;
  pageTitles$!: Observable<PageTitle[]>;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly alertService: AlertService,
    private readonly pageApiService: PageApiService,
    private readonly userConfigService: UserConfigService
  ) {}

  ngOnInit() {
    this.alertService.clearAlertMessages();

    this.options = buildCustomReviewOptionsFromParamMap(this.route.snapshot.paramMap);
    this.pageTitles$ = this.pageApiService.findPageTitlesToReviewByCustomType({ ...this.options });
  }

  get wikipediaUrl(): string {
    return `https://${this.userConfigService.lang()}.wikipedia.org/wiki/`;
  }
}
