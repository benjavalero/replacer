import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-review-subtype',
  templateUrl: './review-subtype.component.html',
  styles: []
})
export class ReviewSubtypeComponent implements OnInit {
  @Input() type: string;
  @Input() subtype: string;

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit(): void {}
}