import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';

@Component({
  selector: 'app-stats',
  templateUrl: './stats.component.html',
  styleUrls: []
})
export class StatsComponent implements OnInit {
  numReplacements: string;
  numArticles: string;
  numReviewedArticles: string;

  constructor(private httpClient: HttpClient) {}

  ngOnInit() {
    this.findNumReplacements();
    this.findNumArticles();
    this.findNumReviewedArticles();
  }

  private findNumReplacements() {
    this.httpClient
      .get<string>(`${environment.apiUrl}/article/count/replacements`)
      .subscribe(res => {
        this.numReplacements = res;
      });
  }

  private findNumArticles() {
    this.httpClient
      .get<string>(`${environment.apiUrl}/article/count/replacements/to-review`)
      .subscribe(res => {
        this.numArticles = res;
      });
  }

  private findNumReviewedArticles() {
    this.httpClient
      .get<string>(`${environment.apiUrl}/article/count/replacements/reviewed`)
      .subscribe(res => {
        this.numReviewedArticles = res;
      });
  }
}
