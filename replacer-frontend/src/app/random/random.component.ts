import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';
import { ArticleReview } from './article-review';

@Component({
  selector: 'app-random',
  templateUrl: './random.component.html',
  styleUrls: ['./random.component.css']
})
export class RandomComponent implements OnInit {
  article = {} as ArticleReview;

  constructor(private httpClient: HttpClient) {}

  ngOnInit(): void {
    const word = ''; // TODO : Leer de la ruta
    this.findRandomArticle(word);
  }

  private findRandomArticle(word: string) {
    // TODO : Usar el parámetro word de la ruta
    this.httpClient
      .get<ArticleReview>(`${environment.apiUrl}/article/random`)
      .subscribe(res => {
        this.article = res;
      });
  }
}
