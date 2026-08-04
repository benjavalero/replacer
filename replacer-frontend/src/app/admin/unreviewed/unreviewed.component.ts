import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { PageCount } from '../../api/models/page-count';
import { ReplacementApiService } from '../../api/services/replacement-api.service';
import { UserConfigService } from '../../core/services/user-config.service';

@Component({
  selector: 'app-unreviewed',
  imports: [CommonModule, RouterModule],
  templateUrl: './unreviewed.component.html',
  styleUrls: []
})
export class UnreviewedComponent implements OnInit {
  private replacementApiService = inject(ReplacementApiService);
  private userConfigService = inject(UserConfigService);

  unreviewed$!: Observable<PageCount[]>;

  ngOnInit() {
    this.unreviewed$ = this.replacementApiService.countNotReviewedReplacementsGroupedByPage();
  }

  get wikipediaUrl(): string {
    return `https://${this.userConfigService.lang()}.wikipedia.org/wiki/`;
  }
}
