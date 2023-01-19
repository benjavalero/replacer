import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { UserConfigService } from 'src/app/core/user/user-config.service';
import { PageCount } from '../../api/models/page-count';
import { ReplacementsService } from '../../api/services/replacements.service';

@Component({
  selector: 'app-unreviewed',
  templateUrl: './unreviewed.component.html',
  styleUrls: []
})
export class UnreviewedComponent implements OnInit {
  unreviewed$!: Observable<PageCount[]>;

  constructor(private replacementService: ReplacementsService, private userConfigService: UserConfigService) {}

  ngOnInit() {
    this.unreviewed$ = this.replacementService.countNotReviewedGroupedByPage();
  }

  get wikipediaUrl(): string {
    return `https://${this.userConfigService.lang}.wikipedia.org/wiki/`;
  }
}
