package es.bvalero.replacer.parse;

import java.util.Date;

public interface ArticleHandler {

    void processArticle(String articleContent, String articleTitle, Date articleTimestamp);

}
