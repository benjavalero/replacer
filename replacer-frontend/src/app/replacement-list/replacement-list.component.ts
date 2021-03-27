import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Observable } from 'rxjs';
import { ReplacementCountList } from './replacement-list.model';
import { ReplacementListService } from './replacement-list.service';

@Component({
  selector: 'app-replacement-list',
  templateUrl: './replacement-list.component.html',
  styleUrls: []
})
export class ReplacementListComponent implements OnInit {
  counts$: Observable<ReplacementCountList[]>;

  constructor(private titleService: Title, private replacementService: ReplacementListService) {
    this.counts$ = this.replacementService.counts$;
  }

  ngOnInit() {
    this.titleService.setTitle('Replacer - Lista de reemplazos');

    this.replacementService.loadCountsFromServer();
  }
}
