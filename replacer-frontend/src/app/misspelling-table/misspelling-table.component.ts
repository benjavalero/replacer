import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormControl } from '@angular/forms';

import { environment } from '../../environments/environment';

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
  misspellingCount: ReplacementCount[];
  filter = new FormControl('');
  page: number;
  pageSize: number;
  collectionSize: number;

  constructor(private httpClient: HttpClient) {
    this.misspellingCount = [];
    this.page = 1;
    this.pageSize = 10;
    this.collectionSize = this.misspellingCount.length;
  }

  ngOnInit() {
    this.findMisspellingCount();
  }

  get misspellings(): ReplacementCount[] {
    return this.misspellingCount
      .filter(misspelling =>
        misspelling.text.toLowerCase().includes(this.filter.value.toLowerCase())
      )
      .slice(
        (this.page - 1) * this.pageSize,
        (this.page - 1) * this.pageSize + this.pageSize
      );
  }

  private findMisspellingCount() {
    this.httpClient
      .get<ReplacementCount[]>(
        `${environment.apiUrl}/statistics/count/misspellings`
      )
      .subscribe(res => {
        this.misspellingCount = res;
        this.collectionSize = this.misspellingCount.length;
      });
  }
}
