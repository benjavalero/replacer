package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.oauth.OAuth10aService;

public interface IWikipediaFacade {

    OAuth10aService getOAuthService();

    String getArticleContent(String articleTitle) throws WikipediaException;

    void editArticleContent(String articleTitle, String articleContent, String editSummary)
            throws WikipediaException;

}
