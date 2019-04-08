import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

import { environment } from '../../environments/environment';
import { ArticleReview } from './article-review';
import { AlertMessage } from './alert-message';

@Component({
  selector: 'app-random',
  templateUrl: './random.component.html',
  styleUrls: ['./random.component.css']
})
export class RandomComponent implements OnInit {
  loading = true;
  word: string;
  article = {} as ArticleReview;
  messages: AlertMessage[] = [];

  constructor(private httpClient: HttpClient, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.word = this.route.snapshot.paramMap.get('word');
    this.findRandomArticle();
  }

  private addMessage(message: AlertMessage): void {
    this.messages.push(message);
  }

  private findRandomArticle(): void {
    this.addMessage({
      type: 'info',
      message: 'Buscando artículo con reemplazos…'
    });

    this.httpClient
      .get<ArticleReview>(
        `${environment.apiUrl}/article/random/${this.word || ''}`
      )
      .subscribe((res: ArticleReview) => {
        this.messages = [];
        if (res.content) {
          this.article = res;
          this.loading = false;
        } else if (res.title) {
          this.addMessage({ type: 'info', message: res.title });
        }
      });
  }

  onSaving(message: AlertMessage) {
    this.loading = true;
    this.addMessage(message);
  }

  onSaved(message: AlertMessage) {
    this.addMessage(message);
    this.findRandomArticle();
  }
}
