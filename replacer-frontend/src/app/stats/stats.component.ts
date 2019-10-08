import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';

import { ArticleService } from '../article/article.service';
import { AlertService } from '../alert/alert.service';
import { ReviewerCount } from './reviewer-count.model';

@Component({
  selector: 'app-stats',
  templateUrl: './stats.component.html',
  styleUrls: []
})
export class StatsComponent implements OnInit {
  numReplacements: number;
  numNotReviewed: number;
  numReviewed: number;
  numReviewedGrouped: ReviewerCount[] = [];

  constructor(private articleService: ArticleService, private alertService: AlertService, private titleService: Title) { }

  ngOnInit() {
    this.titleService.setTitle('Replacer - Estadísticas');
    this.alertService.clearAlertMessages();

    this.findNumReplacements();
    this.findNumNotReviewed();
    this.findNumReviewed();
    this.findNumReviewedGrouped();
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

  private findNumReviewedGrouped() {
    this.articleService.findNumReviewedByReviewer().subscribe((res: ReviewerCount[]) => this.numReviewedGrouped = res);
  }
}
