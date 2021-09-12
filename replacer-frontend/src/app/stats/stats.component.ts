import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { UserConfigService } from '../user/user-config.service';
import { ReplacementCount } from './count.model';
import { ReviewerCount } from './reviewer-count.model';
import { StatsService } from './stats.service';

@Component({
  selector: 'app-stats',
  templateUrl: './stats.component.html',
  styleUrls: []
})
export class StatsComponent implements OnInit {
  lang!: string;
  numReviewed$!: Observable<ReplacementCount>;
  numNotReviewed$!: Observable<ReplacementCount>;
  numReviewedGrouped$!: Observable<ReviewerCount[]>;

  constructor(
    private statsService: StatsService,
    private titleService: Title,
    private userConfigService: UserConfigService
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Estadísticas');

    this.lang = this.userConfigService.lang;

    this.numReviewed$ = this.statsService.findNumReviewed$();
    this.numNotReviewed$ = this.statsService.findNumNotReviewed$();
    this.numReviewedGrouped$ = this.statsService.findNumReviewedByReviewer$();
  }
}
