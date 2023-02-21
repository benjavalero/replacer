import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { ReplacementCount } from '../api/models/replacement-count';
import { ReviewerCount } from '../api/models/reviewer-count';
import { ReplacementService } from '../api/services/replacement.service';
import { UserConfigService } from '../core/user/user-config.service';
import { AlertComponent } from '../shared/alert/alert.component';

@Component({
  standalone: true,
  selector: 'app-stats',
  imports: [CommonModule, AlertComponent],
  templateUrl: './stats.component.html',
  styleUrls: []
})
export class StatsComponent implements OnInit {
  lang!: string;
  numReviewed$!: Observable<ReplacementCount>;
  numNotReviewed$!: Observable<ReplacementCount>;
  numReviewedGrouped$!: Observable<ReviewerCount[]>;

  constructor(
    private replacementService: ReplacementService,
    private titleService: Title,
    private userConfigService: UserConfigService
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Replacer - Estadísticas');

    this.lang = this.userConfigService.lang;

    this.numReviewed$ = this.replacementService.countReplacements({
      reviewed: true
    });
    this.numNotReviewed$ = this.replacementService.countReplacements({
      reviewed: false
    });
    this.numReviewedGrouped$ = this.replacementService.countReplacementsGroupedByReviewer();
  }
}
