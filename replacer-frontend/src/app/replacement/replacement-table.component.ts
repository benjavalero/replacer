import { Component, OnInit, QueryList, ViewChildren } from '@angular/core';

import { ReplacementCountList } from './replacement-count-list.model';
import { ReplacementCount } from './replacement-count.model';
import { ReplacementService } from './replacement.service';
import { AlertService } from '../alert/alert.service';
import { ColumnSortableDirective, SortEvent, compare, compareLocale, SortDirection } from './column-sortable.directive';
import { sleep } from '../sleep';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-replacement-table',
  templateUrl: './replacement-table.component.html',
  styleUrls: ['./replacement-table.component.css']
})
export class ReplacementTableComponent implements OnInit {
  // Sorting
  @ViewChildren(ColumnSortableDirective) headers: QueryList<ColumnSortableDirective>;

  replacementCounts: ReplacementCount[];
  filteredItems: ReplacementCount[];

  // Filter
  filterValue: string;

  // Pagination
  collectionSize: number;
  pageValue: number;
  pageSize: number;

  constructor(private replacementService: ReplacementService, private alertService: AlertService) {
    this.replacementCounts = [];
    this.filteredItems = this.replacementCounts;
    this.filterValue = '';
    this.collectionSize = 0;
    this.pageValue = 1;
    this.pageSize = PAGE_SIZE;
  }

  ngOnInit() {
    this.alertService.addInfoMessage('Cargando estadísticas de reemplazos…');
    this.findReplacementCounts();
  }

  private refreshFilteredItems() {
    // Apply filter
    const filtered = this.replacementCounts.filter(item =>
      this.removeDiacritics(item.subtype).includes(this.removeDiacritics(this.filterValue)));
    this.collectionSize = filtered.length;

    // Paginate
    this.filteredItems = filtered.slice(
      (this.pageValue - 1) * this.pageSize,
      (this.pageValue - 1) * this.pageSize + this.pageSize);
  }

  private removeDiacritics(word: string): string {
    return word.trim().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  }

  private findReplacementCounts() {
    this.replacementService.findReplacementCounts().subscribe((replacementCountLists: ReplacementCountList[]) => {
      // Expand the reduced tree of replacement counts
      replacementCountLists.forEach(rc => {
        rc.l.forEach(c => {
          const count = new ReplacementCount();
          count.type = rc.t;
          count.subtype = c.s;
          count.count = c.c;
          this.replacementCounts.push(count);
        });
      });

      // Initially we sort by type and then by subtype
      this.replacementCounts = this.sort(this.sort(this.replacementCounts, 'subtype', 'asc'), 'type', 'asc');

      this.refreshFilteredItems();

      this.alertService.clearAlertMessages();

      // Mark the column Type as sorted. It takes a little for the headers to be loaded.
      sleep(500).then(() => this.headers.find(header => header.appColumnSortable === 'type').direction = 'asc');
    });
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
      const res = (['type', 'subtype'].includes(column)
        ? compareLocale(a[column], b[column])
        : compare(a[column], b[column]));
      return direction === 'asc' ? res : -res;
    });
  }

}
