import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';

import { AlertService } from '../alert/alert.service';
import { ReviewerCount } from './reviewer-count.model';
import { ReplacementService } from '../replacement/replacement.service';

@Component({
  selector: 'app-stats',
  templateUrl: './stats.component.html',
  styleUrls: []
})
export class StatsComponent implements OnInit {
  numNotReviewed: number;
  numReviewed: number;
  numReviewedGrouped: ReviewerCount[] = [];

  constructor(
    private replacementService: ReplacementService,
    private alertService: AlertService,
    private titleService: Title
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Estadísticas');
    this.alertService.clearAlertMessages();

    this.findNumNotReviewed();
    this.findNumReviewed();
    this.findNumReviewedGrouped();
  }

  private findNumNotReviewed() {
    this.replacementService.findNumNotReviewed().subscribe((res: number) => (this.numNotReviewed = res));
  }

  private findNumReviewed() {
    this.replacementService.findNumReviewed().subscribe((res: number) => (this.numReviewed = res));
  }

  private findNumReviewedGrouped() {
    this.replacementService
      .findNumReviewedByReviewer()
      .subscribe((res: ReviewerCount[]) => (this.numReviewedGrouped = res));
  }
}
