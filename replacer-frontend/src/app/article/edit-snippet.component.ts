import { Component, OnInit, Input } from '@angular/core';
import { ReplacementSuggestion } from './replacement-suggestion.model';

const THRESHOLD = 200; // Number of characters to display around the replacements

@Component({
  selector: 'app-edit-snippet',
  templateUrl: './edit-snippet.component.html',
  styles: [`.pre { white-space: pre-wrap; }`]
})
export class EditSnippetComponent implements OnInit {

  @Input() text: string;
  @Input() start: number;
  @Input() word: string;
  @Input() suggestions: ReplacementSuggestion[];

  private textLeft: string;
  private suggestionSelected: ReplacementSuggestion;
  private textRight: string;

  constructor() { }

  ngOnInit() {
    this.textLeft = this.trimLeft(this.text);
    this.textRight = this.trimRight(this.text);

    // If the original word is not in the suggestions we added it at the first position
    const originalWordSuggested = this.suggestions.map(sug => sug.text).findIndex(word => word === this.word);
    if (originalWordSuggested >= 0) {
      this.suggestionSelected = this.suggestions[originalWordSuggested];
    } else {
      this.suggestions.unshift({
        text: this.word,
        comment: 'no reemplazar'
      });
      this.suggestionSelected = this.suggestions[0];
    }
  }

  private trimLeft(text: string): string {
    const limitLeft = Math.max(0, this.start - THRESHOLD);
    return (limitLeft ? '[...] ' : '') + text.slice(limitLeft, this.start);
  }

  private trimRight(text: string): string {
    const end = this.start + this.word.length;
    const limitRight = Math.min(end + THRESHOLD, text.length);
    return text.slice(end, limitRight) + (limitRight === text.length ? '' : ' [...]');
  }

}
