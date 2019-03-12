import {
  Component,
  Input,
  Output,
  EventEmitter,
  AfterViewInit
} from '@angular/core';

@Component({
  selector: 'app-misspelling-replacer',
  templateUrl: './misspelling-replacer.component.html',
  styleUrls: ['./misspelling-replacer.component.css']
})
export class MisspellingReplacerComponent implements AfterViewInit {
  @Input() start: number;
  @Input() text: string;
  @Input() comment: string;
  @Input() suggestion: string;
  @Output() replaced = new EventEmitter<boolean>();

  newText: string;
  fixed = false;

  constructor() {}

  ngAfterViewInit(): void {
    this.newText = this.text;
  }

  onClick() {
    this.newText = this.fixed ? this.text : this.suggestion;
    this.fixed = !this.fixed;
  }
}
