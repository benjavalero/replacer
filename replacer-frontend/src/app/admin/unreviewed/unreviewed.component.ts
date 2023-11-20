import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { UserConfigService } from 'src/app/core/user/user-config.service';
import { PageCount } from '../../api/models/page-count';
import { ReplacementApiService } from '../../api/services/replacement-api.service';

@Component({
  standalone: true,
  selector: 'app-unreviewed',
  imports: [CommonModule, RouterModule],
  templateUrl: './unreviewed.component.html',
  styleUrls: []
})
export class UnreviewedComponent implements OnInit {
  unreviewed$!: Observable<PageCount[]>;

  constructor(
    private replacementApiService: ReplacementApiService,
    private userConfigService: UserConfigService
  ) {}

  ngOnInit() {
    this.unreviewed$ = this.replacementApiService.countNotReviewedReplacementsGroupedByPage();
  }

  get wikipediaUrl(): string {
    return `https://${this.userConfigService.lang}.wikipedia.org/wiki/`;
  }
}
