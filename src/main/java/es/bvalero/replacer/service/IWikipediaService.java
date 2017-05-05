package es.bvalero.replacer.service;

public interface IWikipediaService {

    String getArticleContent(String articleTitle);

    void editArticleContent(String articleTitle, String articleContent, String editSummary);

}
