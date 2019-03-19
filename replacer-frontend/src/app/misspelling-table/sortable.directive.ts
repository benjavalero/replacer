import {
  Directive,
  EventEmitter,
  HostBinding,
  HostListener,
  Input,
  Output
} from '@angular/core';

export type SortDirection = 'asc' | 'desc' | '';
const rotate: { [key: string]: SortDirection } = {
  '': 'asc',
  asc: 'desc',
  desc: 'asc'
};
export const compare = (v1, v2) => (v1 < v2 ? -1 : v1 > v2 ? 1 : 0);

export interface SortEvent {
  column: string;
  direction: SortDirection;
}

@Directive({
  selector: '[appSortable]'
})
export class SortableDirective {
  @Input() appSortable: string;
  @Output() sort = new EventEmitter<SortEvent>();

  @HostBinding('class') direction: SortDirection = '';

  @HostListener('click') onClick() {
    this.direction = rotate[this.direction];
    this.sort.emit({ column: this.appSortable, direction: this.direction });
  }

  constructor() {}
}
