import { Component, OnInit, QueryList, ViewChildren } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormControl } from '@angular/forms';

import { environment } from '../../environments/environment';

import { SortableDirective, SortEvent, compare } from './sortable.directive';

interface ReplacementCount {
  text: string;
  count: number;
}

@Component({
  selector: 'app-misspelling-table',
  templateUrl: './misspelling-table.component.html',
  styleUrls: ['./misspelling-table.component.css']
})
export class MisspellingTableComponent implements OnInit {
  @ViewChildren(SortableDirective) headers: QueryList<SortableDirective>;

  misspellings: ReplacementCount[];
  filter = new FormControl('');
  page: number;
  pageSize: number;
  collectionSize: number;

  constructor(private httpClient: HttpClient) {
    this.misspellings = [];
    this.page = 1;
    this.pageSize = 10;
    this.collectionSize = this.misspellings.length;
  }

  ngOnInit() {
    this.findMisspellings();
  }

  get filteredMisspellings(): ReplacementCount[] {
    return this.misspellings
      .filter(misspelling =>
        misspelling.text.toLowerCase().includes(this.filter.value.toLowerCase())
      )
      .slice(
        (this.page - 1) * this.pageSize,
        (this.page - 1) * this.pageSize + this.pageSize
      );
  }

  private findMisspellings() {
    this.httpClient
      .get<ReplacementCount[]>(
        `${environment.apiUrl}/article/count/misspellings`
      )
      .subscribe(res => {
        this.misspellings = res;
        this.collectionSize = this.misspellings.length;
      });
  }

  onSort({ column, direction }: SortEvent) {
    // Resetting other headers
    this.headers.forEach(header => {
      if (header.appSortable !== column) {
        header.direction = '';
      }
    });

    // Sorting misspellings
    this.misspellings = [...this.misspellings].sort((a, b) => {
      const res = compare(a[column], b[column]);
      return direction === 'asc' ? res : -res;
    });
  }
}
