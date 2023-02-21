import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  standalone: true,
  selector: 'app-review-subtype',
  templateUrl: './review-subtype.component.html',
  styles: []
})
export class ReviewSubtypeComponent {
  @Input() kind!: string;
  @Input() subtype!: string;

  constructor(public activeModal: NgbActiveModal) {}
}
