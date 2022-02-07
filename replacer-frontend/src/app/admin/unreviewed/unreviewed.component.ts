import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { UserConfigService } from 'src/app/user/user-config.service';
import { PageCount } from './page-count.model';
import { UnreviewedService } from './unreviewed.service';

@Component({
  selector: 'app-unreviewed',
  templateUrl: './unreviewed.component.html',
  styleUrls: []
})
export class UnreviewedComponent implements OnInit {
  unreviewed$!: Observable<PageCount[]>;

  constructor(private unreviewedService: UnreviewedService, private userConfigService: UserConfigService) {}

  ngOnInit() {
    this.unreviewed$ = this.unreviewedService.findPagesWithMostUnreviewedReplacements$();
  }

  get wikipediaUrl(): string {
    return `https://${this.userConfigService.lang}.wikipedia.org/wiki/`;
  }
}
