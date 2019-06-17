import { Component, OnInit } from '@angular/core';

import { ArticleService } from '../article/article.service';

@Component({
  selector: 'app-stats',
  templateUrl: './stats.component.html',
  styleUrls: []
})
export class StatsComponent implements OnInit {
  numReplacements: number;
  numNotReviewed: number;
  numReviewed: number;

  constructor(private articleService: ArticleService) { }

  ngOnInit() {
    this.findNumReplacements();
    this.findNumNotReviewed();
    this.findNumReviewed();
  }

  private findNumReplacements() {
    this.articleService.findNumReplacements().subscribe((res: number) => this.numReplacements = res);
  }

  private findNumNotReviewed() {
    this.articleService.findNumNotReviewed().subscribe((res: number) => this.numNotReviewed = res);
  }

  private findNumReviewed() {
    this.articleService.findNumReviewed().subscribe((res: number) => this.numReviewed = res);
  }
}
