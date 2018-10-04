package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.oauth.OAuth10aService;

public interface IWikipediaFacade {

    String TOKEN_REQUEST = "requestToken";
    String TOKEN_ACCESS = "accessToken";
    String TOKEN_VERIFIER = "oauth_verifier";

    OAuth10aService getOAuthService();

    String getArticleContent(String articleTitle) throws WikipediaException;

    void editArticleContent(String articleTitle, String articleContent, String editSummary)
            throws WikipediaException;

}
