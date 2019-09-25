import { Component, OnInit, QueryList, ViewChildren, Input } from '@angular/core';

import { PAGE_SIZE } from '../app-const';
import { ReplacementCount } from './replacement-count-list.model';
import { ColumnSortableDirective, SortEvent, compare, compareLocale, SortDirection } from './column-sortable.directive';
import { sleep } from '../sleep';

@Component({
  selector: 'app-replacement-table',
  templateUrl: './replacement-table.component.html',
  styleUrls: ['./replacement-table.component.css']
})
export class ReplacementTableComponent implements OnInit {
  // Sorting
  @ViewChildren(ColumnSortableDirective) headers: QueryList<ColumnSortableDirective>;

  @Input() type: string;
  @Input() replacementCounts: ReplacementCount[];

  filteredItems: ReplacementCount[];

  // Filter
  filtrable: boolean;
  filterValue: string;

  // Pagination
  paginable: boolean;
  collectionSize: number;
  pageValue: number;
  pageSize: number;

  constructor() {
    this.replacementCounts = [];
    this.filteredItems = this.replacementCounts;
    this.filtrable = false;
    this.filterValue = '';
    this.paginable = false;
    this.collectionSize = 0;
    this.pageValue = 1;
    this.pageSize = PAGE_SIZE;
  }

  ngOnInit() {
    this.filtrable = this.replacementCounts.length > PAGE_SIZE;
    this.paginable = this.replacementCounts.length > PAGE_SIZE;

    // Initially we sort by subtype
    this.replacementCounts = this.sort(this.replacementCounts, 's', 'asc');

    this.refreshFilteredItems();

    // Mark the column Subtype as sorted. It takes a little for the headers to be loaded.
    sleep(500).then(() => this.headers.find(header => header.appColumnSortable === 's').direction = 'asc');
  }

  private refreshFilteredItems() {
    // Apply filter (empty filter is like applying no filter)
    this.filteredItems = this.replacementCounts.filter(item =>
      this.removeDiacritics(item.s).includes(this.removeDiacritics(this.filterValue)));
    this.collectionSize = this.filteredItems.length;

    // Paginate
    if (this.paginable) {
      this.filteredItems = this.filteredItems.slice(
        (this.pageValue - 1) * this.pageSize,
        (this.pageValue - 1) * this.pageSize + this.pageSize);
    }
  }

  private removeDiacritics(word: string): string {
    return word.trim().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  }

  get filter(): string {
    return this.filterValue;
  }

  set filter(value: string) {
    this.filterValue = value;
    this.page = 1; // Reset page
    this.refreshFilteredItems();
  }

  get page(): number {
    return this.pageValue;
  }

  set page(value: number) {
    this.pageValue = value;
    this.refreshFilteredItems();
  }

  onSort({ column, direction }: SortEvent) {
    // Resetting other headers
    this.headers.forEach(header => {
      if (header.appColumnSortable !== column) {
        header.direction = '';
      }
    });

    // Sorting misspellings
    this.replacementCounts = this.sort(this.replacementCounts, column, direction);

    this.refreshFilteredItems();
  }

  private sort(items: ReplacementCount[], column: string, direction: SortDirection) {
    return [...items].sort((a, b) => {
      const res = (column === 's'
        ? compareLocale(a[column], b[column])
        : compare(a[column], b[column]));
      return direction === 'asc' ? res : -res;
    });
  }

}
