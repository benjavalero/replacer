import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { ReplacementApiService } from '../api/services/replacement-api.service';
import { UserConfigService } from '../core/services/user-config.service';
import { AlertComponent } from '../shared/alerts/alert-container/alert/alert.component';

@Component({
  selector: 'app-stats',
  imports: [AlertComponent],
  templateUrl: './stats.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatsComponent {
  private replacementApiService = inject(ReplacementApiService);
  private userConfigService = inject(UserConfigService);

  lang = this.userConfigService.lang;
  numReviewed = toSignal(this.replacementApiService.countReplacements({ reviewed: true }));
  numNotReviewed = toSignal(this.replacementApiService.countReplacements({ reviewed: false }));
  numReviewedGrouped = toSignal(this.replacementApiService.countReviewedReplacementsGroupedByReviewer());

  constructor() {
    inject(Title).setTitle('Replacer - Estadísticas');
  }
}
