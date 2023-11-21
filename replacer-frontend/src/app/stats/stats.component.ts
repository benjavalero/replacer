import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { ReplacementCount } from '../api/models/replacement-count';
import { ReviewerCount } from '../api/models/reviewer-count';
import { ReplacementApiService } from '../api/services/replacement-api.service';
import { UserConfigService } from '../core/user/user-config.service';
import { AlertComponent } from '../shared/alerts/alert-container/alert/alert.component';

@Component({
  standalone: true,
  selector: 'app-stats',
  imports: [CommonModule, AlertComponent],
  templateUrl: './stats.component.html',
  styleUrls: []
})
export class StatsComponent implements OnInit {
  lang = this.userConfigService.lang;
  numReviewed$!: Observable<ReplacementCount>;
  numNotReviewed$!: Observable<ReplacementCount>;
  numReviewedGrouped$!: Observable<ReviewerCount[]>;

  constructor(
    private replacementApiService: ReplacementApiService,
    private titleService: Title,
    private userConfigService: UserConfigService
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Estadísticas');

    this.numReviewed$ = this.replacementApiService.countReplacements({
      reviewed: true
    });
    this.numNotReviewed$ = this.replacementApiService.countReplacements({
      reviewed: false
    });
    this.numReviewedGrouped$ = this.replacementApiService.countReviewedReplacementsGroupedByReviewer();
  }
}
