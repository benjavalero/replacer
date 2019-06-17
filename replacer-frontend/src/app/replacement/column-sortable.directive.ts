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
export const compareLocale = (v1: string, v2: string) => v1.localeCompare(v2, 'es', { sensitivity: 'base' });

export interface SortEvent {
  column: string;
  direction: SortDirection;
}

@Directive({
  selector: '[appColumnSortable]'
})
export class ColumnSortableDirective {
  @Input() appColumnSortable: string;
  @Output() sort = new EventEmitter<SortEvent>();

  @HostBinding('class') direction: SortDirection = '';

  @HostListener('click') onClick() {
    this.direction = rotate[this.direction];
    this.sort.emit({ column: this.appColumnSortable, direction: this.direction });
  }

  constructor() { }
}
