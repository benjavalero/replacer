import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { ReplacementApiService } from '../../api/services/replacement-api.service';
import { UserConfigService } from '../../core/services/user-config.service';

@Component({
  selector: 'app-unreviewed',
  imports: [RouterLink],
  templateUrl: './unreviewed.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UnreviewedComponent {
  private userConfigService = inject(UserConfigService);

  unreviewed = toSignal(inject(ReplacementApiService).countNotReviewedReplacementsGroupedByPage());

  get wikipediaUrl(): string {
    return `https://${this.userConfigService.lang()}.wikipedia.org/wiki/`;
  }
}
