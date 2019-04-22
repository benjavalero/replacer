// https://stackblitz.com/edit/angular-dynamic-content-viewer

import {
  Component,
  ComponentFactory,
  ComponentFactoryResolver,
  ComponentRef,
  DoCheck,
  ElementRef,
  EventEmitter,
  Injector,
  Input,
  OnDestroy,
  Output,
  ViewChild
} from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { environment } from '../../environments/environment';
import { MisspellingReplacerComponent } from '../misspelling-replacer/misspelling-replacer.component';
import { ArticleReview } from '../random/article-review';
import { ArticleReplacement } from '../random/article-replacements';
import { AlertMessage } from '../random/alert-message';
import { AuthenticationService } from '../authentication/authentication.service';

export const replacerComponents = [MisspellingReplacerComponent];
const THRESHOLD = 200; // Number of characters to display between replacements
const REPLACER_REGEX = new RegExp(
  '<app-misspelling-replacer.+?</app-misspelling-replacer>',
  'g'
);

@Component({
  selector: 'app-content-editor',
  templateUrl: './content-editor.component.html',
  styleUrls: ['./content-editor.component.css']
})
export class ContentEditorComponent implements DoCheck, OnDestroy {
  private articleTitle: string;
  private originalContent: string;
  private escapedContent: string;
  private trimContent: boolean;

  // Dynamic components
  @ViewChild('articleContent', { read: ElementRef }) hostElement: ElementRef;
  private embeddedComponentFactories: Map<
    string,
    ComponentFactory<any>
  > = new Map();
  private embeddedComponents: ComponentRef<any>[] = [];

  @Output() saving = new EventEmitter<AlertMessage>();
  @Output() saved = new EventEmitter<AlertMessage>();

  constructor(
    private httpClient: HttpClient,
    private authenticationService: AuthenticationService,
    componentFactoryResolver: ComponentFactoryResolver,
    private injector: Injector
  ) {
    replacerComponents.forEach(component => {
      const factory = componentFactoryResolver.resolveComponentFactory(
        component
      );
      this.embeddedComponentFactories.set(factory.selector, factory);
    });
  }

  ngDoCheck() {
    this.embeddedComponents.forEach(comp =>
      comp.changeDetectorRef.detectChanges()
    );
  }

  ngOnDestroy() {
    // Destroy these components else there will be memory leaks
    this.embeddedComponents.forEach(comp => comp.destroy());
    this.embeddedComponents.length = 0;
  }

  @Input()
  set article(article: ArticleReview) {
    this.ngOnDestroy();
    if (article.title) {
      this.build(article);
    }
  }

  private build(article: ArticleReview) {
    this.articleTitle = article.title;
    this.originalContent = article.content;
    this.trimContent = article.trimText;
    this.escapedContent = this.addReplacements(
      article.content,
      article.replacements
    );

    this.hostElement.nativeElement.innerHTML = this.escapedContent;

    // Build the dynamic components
    this.embeddedComponentFactories.forEach((factory, selector) => {
      const embeddedComponentElements = this.hostElement.nativeElement.querySelectorAll(
        selector
      );

      for (const element of embeddedComponentElements as any) {
        const projectableNodes = [
          Array.prototype.slice.call(element.childNodes)
        ];

        const embeddedComponent = factory.create(
          this.injector,
          projectableNodes,
          element
        );

        // Apply inputs into the dynamic component
        // Only static ones work here since this is the only time they're set
        for (const attr of (element as any).attributes) {
          embeddedComponent.instance[attr.nodeName] = attr.nodeValue;
        }
        this.embeddedComponents.push(embeddedComponent);
      }
    });
  }

  private addReplacements(
    content: string,
    replacements: ArticleReplacement[]
  ): string {
    // Create the replacement buttons and insert them in the text
    let articleContent = content;
    replacements.forEach(replacement => {
      if (replacement.type === 'MISSPELLING') {
        articleContent = this.replaceText(
          articleContent,
          replacement.start,
          replacement.text,
          this.createMisspellingReplacement(replacement)
        );
      }
    });

    // We could insert the text with "textContent" but we cannot add the buttons later
    // We need to encode the text and insert it with "innerHTML" instead
    articleContent = this.htmlEscape(articleContent);

    // So we "decode" only the buttons
    articleContent = articleContent.replace(
      /&lt;app-misspelling-replacer(.+?)&gt;/g,
      '<app-misspelling-replacer$1>'
    );
    articleContent = articleContent.replace(
      /&lt;\/app-misspelling-replacer&gt;/g,
      '</app-misspelling-replacer>'
    );

    // "Trim" the parts with no replacements
    if (this.trimContent) {
      articleContent = this.trimText(articleContent);
    }

    return articleContent;
  }

  private createMisspellingReplacement(
    replacement: ArticleReplacement
  ): string {
    return `<app-misspelling-replacer start="${replacement.start}" text="${
      replacement.text
      }" comment="${replacement.comment}" suggestion="${
      replacement.suggestion
      }"></app-misspelling-replacer>`;
  }

  private replaceText(
    fullText: string,
    position: number,
    currentText: string,
    newText: string
  ): string {
    // Force the cast to number as it seems the position comes as a string
    position = +position;

    return (
      fullText.slice(0, position) +
      newText +
      fullText.slice(position + currentText.length)
    );
  }

  private htmlEscape(str: string): string {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  private trimText(text: string): string {
    let result = '';

    let lastEnd = 0;
    let m: RegExpExecArray;
    // tslint:disable-next-line: no-conditional-assignment
    while ((m = REPLACER_REGEX.exec(text))) {
      const matchText = m[0];
      const matchStart = m.index;
      const matchEnd = m.index + matchText.length;
      const textBefore = text.substring(lastEnd, matchStart);

      if (lastEnd === 0) {
        result +=
          textBefore.length <= THRESHOLD
            ? textBefore
            : '...' + textBefore.substring(textBefore.length - THRESHOLD);
      } else {
        result +=
          textBefore.length <= THRESHOLD * 2
            ? textBefore
            : textBefore.substring(0, THRESHOLD) +
            '... <hr /> ...' +
            textBefore.substring(textBefore.length - THRESHOLD);
      }
      result += matchText;

      lastEnd = matchEnd;
    }

    // Append the rest after the last button
    const rest = text.substring(lastEnd);
    result +=
      rest.length <= THRESHOLD ? rest : rest.substring(0, THRESHOLD) + '...';

    return result;
  }

  onSaveChanges() {
    let replacers: MisspellingReplacerComponent[] = [];
    this.embeddedComponents.forEach(comp => {
      replacers.push(comp.instance);
    });

    // Sort by inverse position
    replacers = replacers.sort((r1, r2) => r2.start - r1.start);

    // Take the original text and replace the replacements
    let textToSave = this.originalContent;
    replacers.forEach(replacer => {
      if (replacer.text !== replacer.newText) {
        textToSave = this.replaceText(
          textToSave,
          replacer.start,
          replacer.text,
          replacer.newText
        );
      }
    });

    this.saving.emit({
      type: 'info',
      message: `Guardando cambios en «${this.articleTitle}»…`
    });

    let params = new HttpParams();
    params = params.append('token', this.authenticationService.accessToken.token);
    params = params.append('tokenSecret', this.authenticationService.accessToken.tokenSecret);
    params = params.append('title', this.articleTitle);

    if (textToSave === this.originalContent) {
      // Send an empty text in the request body
      this.httpClient.put<boolean>(`${environment.apiUrl}/article`, ' ', { params })
        .subscribe(res => {
          this.saved.emit({
            type: 'success',
            message: `Artículo «${this.articleTitle}» marcado como revisado`
          });
        });
    } else {
      this.httpClient.put<boolean>(`${environment.apiUrl}/article`, textToSave, { params })
        .subscribe(res => {
          this.saved.emit({
            type: 'success',
            message: `Artículo «${this.articleTitle}» editado con éxito`
          });
        });
    }
  }
}
