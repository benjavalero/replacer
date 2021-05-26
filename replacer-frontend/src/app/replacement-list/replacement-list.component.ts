import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ReplacementCountList } from './replacement-list.model';
import { ReplacementListService } from './replacement-list.service';

@Component({
  selector: 'app-replacement-list',
  templateUrl: './replacement-list.component.html',
  styleUrls: []
})
export class ReplacementListComponent implements OnInit {
  typeCounts: ReplacementCountList[];

  constructor(private titleService: Title, private replacementService: ReplacementListService) {
    this.typeCounts = [];

    this.replacementService.counts$.subscribe((counts: ReplacementCountList[]) => {
      // We want to keep the components created in the ngFor template
      for (let i = 0; i < counts.length; i++) {
        let found = false;
        for (let j = 0; j < this.typeCounts.length; j++) {
          if (this.typeCounts[j].t === counts[i].t) {
            this.typeCounts[j].l = counts[i].l;
            found = true;
            break;
          }
        }
        if (!found) {
          // Insert the new type count
          this.typeCounts.splice(i, 0, counts[i]);
        }
      }

      // Inverse check to remove obsolete type counts not existing in the new value
      for (let i = 0; i < this.typeCounts.length; i++) {
        let found = false;
        for (let j = 0; j < counts.length; j++) {
          if (this.typeCounts[i].t === counts[j].t) {
            found = true;
            break;
          }
        }
        if (!found) {
          this.typeCounts[i].l = [];
        }
      }
    });
  }

  ngOnInit() {
    this.titleService.setTitle('Replacer - Lista de reemplazos');
  }
}
