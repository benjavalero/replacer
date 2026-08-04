import { Component, Input, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  standalone: true,
  selector: 'app-review-subtype',
  templateUrl: './review-subtype.component.html',
  styles: []
})
export class ReviewSubtypeComponent {
  activeModal = inject(NgbActiveModal);

  @Input() kind!: string;
  @Input() subtype!: string;
}
