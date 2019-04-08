import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

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
  article = {} as ArticleReview;
  messages: AlertMessage[] = [];

  constructor(private httpClient: HttpClient) {}

  ngOnInit(): void {
    this.findRandomArticle();
  }

  private addMessage(message: AlertMessage): void {
    this.messages.push(message);
  }

  private findRandomArticle(): void {
    const word = ''; // TODO : Leer de la ruta
    // TODO : Usar el parámetro word de la ruta

    this.addMessage({
      type: 'info',
      message: 'Buscando artículo con reemplazos…'
    });

    this.httpClient
      .get<ArticleReview>(`${environment.apiUrl}/article/random`)
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
