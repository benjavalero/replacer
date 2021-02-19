import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { AlertService } from '../alert/alert.service';
import { ReplacementService } from '../replacement/replacement.service';
import { UserConfigService } from '../user/user-config.service';
import { ReviewerCount } from './reviewer-count.model';

@Component({
  selector: 'app-stats',
  templateUrl: './stats.component.html',
  styleUrls: []
})
export class StatsComponent implements OnInit {
  lang: string;
  numNotReviewed: number;
  numReviewed: number;
  numReviewedGrouped: ReviewerCount[] = [];

  constructor(
    private replacementService: ReplacementService,
    private alertService: AlertService,
    private titleService: Title,
    private userConfigService: UserConfigService
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Estadísticas');
    this.alertService.clearAlertMessages();

    this.lang = this.userConfigService.lang;

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
