import { Component, OnInit, QueryList, ViewChildren } from '@angular/core';

import { FormControl } from '@angular/forms';

import { ReplacementCount } from './replacement-count.model';
import { ReplacementService } from './replacement.service';
import { AlertService } from '../alert/alert.service';

import { ColumnSortableDirective, SortEvent, compare, compareLocale } from './column-sortable.directive';

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

  // Filter
  filter = new FormControl('');

  // Pagination
  collectionSize: number;
  page: number;
  pageSize: number;

  constructor(private replacementService: ReplacementService, private alertService: AlertService) {
    this.replacementCounts = [];
    this.page = 1;
    this.pageSize = PAGE_SIZE;
  }

  ngOnInit() {
    this.alertService.addInfoMessage('Cargando estadísticas de reemplazos…');
    this.findReplacementCounts();
  }

  get filteredCounts(): ReplacementCount[] {
    const filteredCounts = this.replacementCounts.filter(replacementCount =>
      this.removeDiacritics(replacementCount.subtype).includes(this.removeDiacritics(this.filter.value)));
    this.collectionSize = filteredCounts.length;
    return filteredCounts.slice(
      (this.page - 1) * this.pageSize,
      (this.page - 1) * this.pageSize + this.pageSize);
  }

  private removeDiacritics(word: string): string {
    return word.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  }

  private findReplacementCounts() {
    this.replacementService.findReplacementCounts().subscribe((replacementCounts: ReplacementCount[]) => {
      this.replacementCounts = replacementCounts;
      this.collectionSize = this.replacementCounts.length;

      this.alertService.clearAlertMessages();
    });
  }

  onSort({ column, direction }: SortEvent) {
    // Resetting other headers
    this.headers.forEach(header => {
      if (header.appColumnSortable !== column) {
        header.direction = '';
      }
    });

    // Sorting misspellings
    this.replacementCounts = [...this.replacementCounts].sort((a, b) => {
      const res = (column === 'text'
        ? compareLocale(a[column], b[column])
        : compare(a[column], b[column]));
      return direction === 'asc' ? res : -res;
    });
  }
}
