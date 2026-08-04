import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-review-subtype',
  templateUrl: './review-subtype.component.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewSubtypeComponent {
  activeModal = inject(NgbActiveModal);

  @Input() kind!: string;
  @Input() subtype!: string;
}
