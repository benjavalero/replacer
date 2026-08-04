import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { PageApiService } from '../../../api/services/page-api.service';
import { UserConfigService } from '../../../core/services/user-config.service';
import { AlertService } from '../../../shared/alerts/alert.service';
import { ReviewOptions } from '../review-options.model';
import { buildCustomReviewOptionsFromParamMap } from '../review-route-options.util';

@Component({
  selector: 'app-custom-page-list',
  imports: [],
  templateUrl: './custom-page-list.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CustomPageListComponent {
  private readonly alertService = inject(AlertService);
  private readonly userConfigService = inject(UserConfigService);

  readonly options: ReviewOptions;
  readonly pageTitles;

  constructor() {
    const route = inject(ActivatedRoute);
    const pageApiService = inject(PageApiService);

    this.alertService.clearAlertMessages();
    this.options = buildCustomReviewOptionsFromParamMap(route.snapshot.paramMap);
    this.pageTitles = toSignal(pageApiService.findPageTitlesToReviewByCustomType({ ...this.options }));
  }

  get wikipediaUrl(): string {
    return `https://${this.userConfigService.lang()}.wikipedia.org/wiki/`;
  }
}
