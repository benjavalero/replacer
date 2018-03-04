package es.bvalero.replacer.wikipedia;

public interface IWikipediaFacade {

    String getArticleContent(String articleTitle) throws WikipediaException;

    void editArticleContent(String articleTitle, String articleContent, String editSummary)
            throws WikipediaException;

}
