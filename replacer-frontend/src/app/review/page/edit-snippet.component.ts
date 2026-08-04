import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, ViewEncapsulation, effect, inject, input, output } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Replacement } from '../../api/models/replacement';
import { Suggestion } from '../../api/models/suggestion';
import { EditCustomSnippetComponent } from './edit-custom-snippet.component';
import { FixedReplacement, Snippet, getReplacementEnd } from './page-replacement.model';

@Component({
  selector: 'app-edit-snippet',
  imports: [NgClass],
  templateUrl: './edit-snippet.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./edit-snippet.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditSnippetComponent {
  private modalService = inject(NgbModal);

  index = input.required<number>();
  pageText = input.required<string>();
  replacement = input.required<Replacement>();
  // Limits on left and right to edit the snippet as we would clash with other replacements
  limitLeft = input.required<number>();
  limitRight = input.required<number>();

  private suggestionSelectedValue!: Suggestion | null;
  customFix: Snippet | null = null;

  fixed = output<FixedReplacement>();

  constructor() {
    effect(() => {
      const replacement = this.replacement();
      // The first suggestion may match with the original text
      if (replacement.text === replacement.suggestions[0].text && !replacement.suggestions[0].comment) {
        replacement.suggestions[0].comment = 'no reemplazar';
      }
      this.suggestionSelected = replacement.suggestions[0];
    });
  }

  get textLeft(): string {
    return this.trimLeft().text;
  }

  get textRight(): string {
    return this.trimRight().text;
  }

  get suggestionSelected(): Suggestion | null {
    return this.suggestionSelectedValue;
  }

  set suggestionSelected(suggestion: Suggestion | null) {
    this.suggestionSelectedValue = suggestion;
    this.customFix = null;
    const replacement = this.replacement();
    const fixedReplacement = new FixedReplacement(
      this.index(),
      replacement.start,
      replacement.text,
      suggestion && suggestion.text !== replacement.text ? suggestion.text : null
    );
    this.fixed.emit(fixedReplacement);
  }

  onSelectSuggestion(index: number) {
    this.suggestionSelected = this.replacement().suggestions[index];
  }

  private trimLeft(): Snippet {
    const replacement = this.replacement();
    const snippetText = this.pageText().slice(this.limitLeft(), replacement.start);
    return new Snippet(this.limitLeft(), snippetText);
  }

  private trimRight(): Snippet {
    const replacement = this.replacement();
    const snippetText = this.pageText().slice(getReplacementEnd(replacement), this.limitRight());
    return new Snippet(getReplacementEnd(replacement), snippetText);
  }

  onEdit(): void {
    const modalRef = this.modalService.open(EditCustomSnippetComponent, { windowClass: 'snippet-modal' });
    const editableSnippet = this.customFix ? this.customFix : this.buildEditableSnippet();
    modalRef.componentInstance.snippet = editableSnippet;
    modalRef.result.then(
      (result: Snippet) => {
        if (editableSnippet.text !== result.text) {
          this.suggestionSelectedValue = null;
          this.customFix = result;

          const fixedReplacement = new FixedReplacement(
            this.index(),
            result.start,
            this.getOriginalText(),
            result.text
          );
          this.fixed.emit(fixedReplacement);
        }
      },
      () => {
        // Modal dismissed. Nothing to do.
      }
    );
  }

  private buildEditableSnippet(): Snippet {
    return new Snippet(this.limitLeft(), this.textLeft + this.suggestionSelected!.text + this.textRight);
  }

  private getOriginalText(): string {
    return this.textLeft + this.replacement().text + this.textRight;
  }
}
