import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ReplacementApiService } from '../../api/services/replacement-api.service';

@Component({
  selector: 'app-custom-counts',
  imports: [],
  templateUrl: './custom-counts.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CustomCountsComponent {
  customCounts = toSignal(inject(ReplacementApiService).countReviewedGroupedByCustomType());
}
